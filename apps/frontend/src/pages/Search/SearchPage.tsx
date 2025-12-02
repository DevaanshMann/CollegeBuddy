import { useState, useEffect } from 'react';
import { Search, X, UserPlus, UserCheck, UserMinus, Clock, UserX, MoreVertical, ShieldOff } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { apiClient } from '../../api/client';
import { blockingApi } from '../../api/blocking';
import { Avatar, Button, Modal } from '../../components/ui';
import toast from 'react-hot-toast';

type SearchResult = {
  userId: number;
  displayName: string;
  avatarUrl?: string;
  visibility?: string;
  campusDomain?: string;
};

type ConnectionStatus = 'you' | 'connected' | 'pending' | 'connect';

const RECENT_SEARCHES_KEY = 'collegebuddy_recent_searches';
const MAX_RECENT_SEARCHES = 10;

export function SearchPage() {
  const { user } = useAuth();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [recentSearches, setRecentSearches] = useState<string[]>([]);
  const [connections, setConnections] = useState<number[]>([]);
  const [pendingRequests, setPendingRequests] = useState<number[]>([]);
  const [confirmDisconnect, setConfirmDisconnect] = useState<{
    userId: number;
    displayName: string;
  } | null>(null);
  const [confirmBlock, setConfirmBlock] = useState<{
    userId: number;
    displayName: string;
  } | null>(null);
  const [blockedUsers, setBlockedUsers] = useState<number[]>([]);

  useEffect(() => {
    loadRecentSearches();
    loadConnections();
    loadBlockedUsers();
  }, []);

  const loadRecentSearches = () => {
    try {
      const stored = localStorage.getItem(RECENT_SEARCHES_KEY);
      if (stored) {
        setRecentSearches(JSON.parse(stored));
      }
    } catch (error) {
      console.error('Failed to load recent searches:', error);
    }
  };

  const saveRecentSearch = (searchTerm: string) => {
    try {
      const trimmed = searchTerm.trim();
      if (!trimmed) return;

      const updated = [
        trimmed,
        ...recentSearches.filter((s) => s !== trimmed),
      ].slice(0, MAX_RECENT_SEARCHES);

      setRecentSearches(updated);
      localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
    } catch (error) {
      console.error('Failed to save recent search:', error);
    }
  };

  const clearRecentSearches = () => {
    setRecentSearches([]);
    localStorage.removeItem(RECENT_SEARCHES_KEY);
  };

  const removeRecentSearch = (searchTerm: string) => {
    const updated = recentSearches.filter((s) => s !== searchTerm);
    setRecentSearches(updated);
    localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
  };

  async function loadConnections() {
    try {
      const res = await apiClient.get<{
        friends: any[];
        incomingRequests: any[];
        outgoingRequests: any[];
      }>('/connections');

      setConnections(res.friends.map((f: any) => f.userId));
      setPendingRequests(res.outgoingRequests.map((r: any) => r.userId));
    } catch (err) {
      console.error('Failed to load connections:', err);
    }
  }

  async function loadBlockedUsers() {
    try {
      const blocked = await blockingApi.getBlockedUsers();
      setBlockedUsers(blocked.map((b) => b.userId));
    } catch (err) {
      console.error('Failed to load blocked users:', err);
    }
  }

  async function handleSearch(searchTerm?: string) {
    const term = searchTerm || query;
    if (!term.trim()) {
      toast.error('Please enter a search term');
      return;
    }

    setLoading(true);

    try {
      const response = await apiClient.post<any>('/search', {
        query: term.trim(),
      });

      const list: SearchResult[] = Array.isArray(response)
        ? response
        : response?.results ?? [];

      setResults(list);
      saveRecentSearch(term.trim());

      if (list.length === 0) {
        toast.error(`No classmates found for "${term.trim()}"`);
      }
    } catch (err: any) {
      console.error('Search error:', err);
      toast.error(err.message ?? 'Search failed');
    } finally {
      setLoading(false);
    }
  }

  async function handleConnect(userId: number, displayName: string) {
    try {
      await apiClient.post('/connections/request', {
        toUserId: userId,
        message: `Hey ${displayName}, let's connect!`,
      });
      toast.success(`Connection request sent to ${displayName}!`);

      setPendingRequests([...pendingRequests, userId]);
    } catch (err: any) {
      console.error('Send request error:', err);
      toast.error(err.message ?? 'Failed to send request');
    }
  }

  async function handleDisconnect() {
    if (!confirmDisconnect) return;

    try {
      await apiClient.del(`/connections/${confirmDisconnect.userId}`);
      toast.success(`Disconnected from ${confirmDisconnect.displayName}`);
      setConfirmDisconnect(null);

      setConnections(connections.filter((id) => id !== confirmDisconnect.userId));

      // Reload search results to update status
      if (results.length > 0) {
        await handleSearch(query);
      }
    } catch (err: any) {
      console.error('Disconnect error:', err);
      toast.error(err.message ?? 'Failed to disconnect');
      setConfirmDisconnect(null);
    }
  }

  async function handleBlock() {
    if (!confirmBlock) return;

    try {
      await blockingApi.blockUser(confirmBlock.userId);
      toast.success(`Blocked ${confirmBlock.displayName}`);
      setConfirmBlock(null);

      setBlockedUsers([...blockedUsers, confirmBlock.userId]);
    } catch (err: any) {
      console.error('Block error:', err);
      toast.error(err.message ?? 'Failed to block user');
      setConfirmBlock(null);
    }
  }

  async function handleUnblock(userId: number, displayName: string) {
    try {
      await blockingApi.unblockUser(userId);
      toast.success(`Unblocked ${displayName}`);

      setBlockedUsers(blockedUsers.filter((id) => id !== userId));
    } catch (err: any) {
      console.error('Unblock error:', err);
      toast.error(err.message ?? 'Failed to unblock user');
    }
  }

  const getConnectionStatus = (userId: number): ConnectionStatus => {
    // Handle potential type mismatch between userId and user.id
    if (user?.id && Number(userId) === Number(user.id)) {
      console.log('Found your own profile:', userId, user.id);
      return 'you';
    }
    if (connections.includes(userId)) return 'connected';
    if (pendingRequests.includes(userId)) return 'pending';
    return 'connect';
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2">
          Search
        </h1>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Find classmates at your campus
        </p>
      </div>

      {/* Search Input */}
      <div className="relative mb-6">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
        <input
          type="text"
          placeholder="Search by name..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          className="w-full pl-12 pr-4 py-3 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-light-text-primary dark:text-dark-text-primary"
        />
      </div>

      {/* Search Button */}
      <Button
        onClick={() => handleSearch()}
        loading={loading}
        fullWidth
        variant="primary"
        className="mb-8 gap-2"
      >
        <Search className="w-4 h-4" />
        Search
      </Button>

      {/* Recent Searches */}
      {results.length === 0 && recentSearches.length > 0 && (
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary">
              Recent
            </h3>
            <button
              onClick={clearRecentSearches}
              className="text-sm text-blue-500 hover:text-blue-600 font-semibold"
            >
              Clear all
            </button>
          </div>
          <div className="space-y-2">
            {recentSearches.map((search, index) => (
              <button
                key={index}
                onClick={() => {
                  setQuery(search);
                  handleSearch(search);
                }}
                className="w-full flex items-center justify-between p-3 hover:bg-light-surface dark:hover:bg-dark-surface rounded-lg transition-colors group"
              >
                <div className="flex items-center gap-3">
                  <Clock className="w-5 h-5 text-gray-400" />
                  <span className="text-light-text-primary dark:text-dark-text-primary">
                    {search}
                  </span>
                </div>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    removeRecentSearch(search);
                  }}
                  className="opacity-0 group-hover:opacity-100 p-1 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-full transition-opacity"
                >
                  <X className="w-4 h-4 text-gray-500" />
                </button>
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Search Results */}
      {results.length > 0 && (
        <div>
          <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-4">
            Results ({results.length})
          </h3>
          <div className="space-y-3">
            {results.map((result) => {
              const status = getConnectionStatus(result.userId);
              const isBlocked = blockedUsers.includes(result.userId);

              return (
                <div
                  key={result.userId}
                  className="flex items-center justify-between p-4 bg-light-surface dark:bg-dark-surface rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                  {/* User Info */}
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <Avatar
                      src={result.avatarUrl}
                      alt={result.displayName}
                      size="md"
                      fallback={result.displayName}
                    />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="font-semibold text-light-text-primary dark:text-dark-text-primary truncate">
                          {result.displayName}
                        </p>
                        {isBlocked && (
                          <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 border border-red-300 dark:border-red-700">
                            Blocked
                          </span>
                        )}
                      </div>
                      {result.campusDomain && (
                        <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary truncate">
                          @{result.campusDomain}
                        </p>
                      )}
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex-shrink-0 ml-3 flex gap-2">
                    {status === 'you' && (
                      <div className="px-3 py-1.5 text-sm font-semibold rounded-lg border-2 border-blue-500 bg-blue-500 text-white">
                        You
                      </div>
                    )}
                    {status === 'connected' && !isBlocked && (
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() =>
                          setConfirmDisconnect({
                            userId: result.userId,
                            displayName: result.displayName,
                          })
                        }
                        className="gap-2"
                      >
                        <UserCheck className="w-4 h-4" />
                        Connected
                      </Button>
                    )}
                    {status === 'pending' && !isBlocked && (
                      <span className="text-sm text-yellow-500 dark:text-yellow-400 font-medium flex items-center gap-1">
                        <Clock className="w-4 h-4" />
                        Pending
                      </span>
                    )}
                    {status === 'connect' && !isBlocked && (
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() =>
                          handleConnect(result.userId, result.displayName)
                        }
                        className="gap-2"
                      >
                        <UserPlus className="w-4 h-4" />
                        Connect
                      </Button>
                    )}
                    {status !== 'you' && !isBlocked && (
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() =>
                          setConfirmBlock({
                            userId: result.userId,
                            displayName: result.displayName,
                          })
                        }
                        className="gap-2"
                      >
                        <UserX className="w-4 h-4" />
                        Block
                      </Button>
                    )}
                    {isBlocked && (
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => handleUnblock(result.userId, result.displayName)}
                        className="gap-2"
                      >
                        <ShieldOff className="w-4 h-4" />
                        Unblock
                      </Button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Empty State */}
      {results.length === 0 && recentSearches.length === 0 && !loading && (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
            <Search className="w-8 h-8 text-gray-400" />
          </div>
          <h3 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
            Search for classmates
          </h3>
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Enter a name to find students at your campus
          </p>
        </div>
      )}

      {/* Disconnect Confirmation Modal */}
      <Modal
        isOpen={!!confirmDisconnect}
        onClose={() => setConfirmDisconnect(null)}
        title="Disconnect from connection"
      >
        <div className="space-y-4">
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Are you sure you want to disconnect from{' '}
            <strong className="text-light-text-primary dark:text-dark-text-primary">
              {confirmDisconnect?.displayName}
            </strong>
            ? You will need to send a new connection request to reconnect.
          </p>
          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => setConfirmDisconnect(null)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              fullWidth
              onClick={handleDisconnect}
              className="gap-2"
            >
              <UserMinus className="w-4 h-4" />
              Disconnect
            </Button>
          </div>
        </div>
      </Modal>

      {/* Block Confirmation Modal */}
      <Modal
        isOpen={!!confirmBlock}
        onClose={() => setConfirmBlock(null)}
        title="Block User"
      >
        <div className="space-y-4">
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Are you sure you want to block{' '}
            <strong className="text-light-text-primary dark:text-dark-text-primary">
              {confirmBlock?.displayName}
            </strong>
            ? They won't be able to:
          </p>
          <ul className="list-disc list-inside text-sm text-light-text-secondary dark:text-dark-text-secondary space-y-1 ml-2">
            <li>Send you messages</li>
            <li>Send you connection requests</li>
            <li>See your profile in search results</li>
          </ul>
          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => setConfirmBlock(null)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              fullWidth
              onClick={handleBlock}
              className="gap-2"
            >
              <UserX className="w-4 h-4" />
              Block User
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
