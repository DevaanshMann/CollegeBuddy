import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, MessageCircle, UserPlus, TrendingUp, UserMinus } from 'lucide-react';
import { apiClient } from '../../api/client';
import type { DashboardStats, ConnectionRequestDto } from '../../types';
import { Avatar, Button, Modal } from '../../components/ui';
import { clsx } from 'clsx';
import toast from 'react-hot-toast';

type Friend = {
  userId: number;
  displayName: string;
  avatarUrl?: string;
  unreadCount?: number;
};

export function HomePage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats>({
    pendingRequestsCount: 0,
    unreadMessagesCount: 0,
    connectionsCount: 0,
    profileViews: 0,
  });
  const [incomingRequests, setIncomingRequests] = useState<ConnectionRequestDto[]>([]);
  const [friends, setFriends] = useState<Friend[]>([]);
  const [loading, setLoading] = useState(true);
  const [confirmDisconnect, setConfirmDisconnect] = useState<{
    userId: number;
    displayName: string;
  } | null>(null);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const [connectionsData] = await Promise.all([
        apiClient.get<{
          friends: Friend[];
          incomingRequests: ConnectionRequestDto[];
          outgoingRequests: any[];
        }>('/connections'),
      ]);

      setIncomingRequests(connectionsData.incomingRequests || []);
      setFriends(connectionsData.friends || []);

      // Calculate unread messages
      const unreadCount = (connectionsData.friends || []).reduce(
        (sum: number, friend: Friend) => sum + (friend.unreadCount || 0),
        0
      );

      setStats({
        pendingRequestsCount: (connectionsData.incomingRequests || []).length,
        unreadMessagesCount: unreadCount,
        connectionsCount: (connectionsData.friends || []).length,
        profileViews: 0, // Not implemented yet
      });
    } catch (error) {
      console.error('Failed to load dashboard:', error);
      toast.error('Failed to load dashboard data');
      // Set empty data to prevent crashes
      setIncomingRequests([]);
      setFriends([]);
      setStats({
        pendingRequestsCount: 0,
        unreadMessagesCount: 0,
        connectionsCount: 0,
        profileViews: 0,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleAcceptRequest = async (userId: number) => {
    try {
      await apiClient.post('/connections/respond', {
        otherUserId: userId,
        accept: true,
      });
      loadDashboard(); // Reload to update stats
    } catch (error) {
      console.error('Failed to accept request:', error);
    }
  };

  const handleDeclineRequest = async (userId: number) => {
    try {
      await apiClient.post('/connections/respond', {
        otherUserId: userId,
        accept: false,
      });
      loadDashboard(); // Reload to update stats
    } catch (error) {
      console.error('Failed to decline request:', error);
    }
  };

  const handleDisconnect = async () => {
    if (!confirmDisconnect) return;

    try {
      await apiClient.del(`/connections/${confirmDisconnect.userId}`);
      toast.success(`Disconnected from ${confirmDisconnect.displayName}`);
      setConfirmDisconnect(null);
      loadDashboard(); // Reload to update stats
    } catch (err: any) {
      console.error('Disconnect error:', err);
      toast.error(err.message ?? 'Failed to disconnect');
      setConfirmDisconnect(null);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  const statCards = [
    {
      title: 'Connections',
      value: stats.connectionsCount,
      icon: Users,
      color: 'text-blue-500',
      bgColor: 'bg-blue-50',
      onClick: () => navigate('/connections'),
    },
    {
      title: 'Unread Messages',
      value: stats.unreadMessagesCount,
      icon: MessageCircle,
      color: 'text-green-500',
      bgColor: 'bg-green-50',
      onClick: () => navigate('/messages'),
    },
    {
      title: 'Pending Requests',
      value: stats.pendingRequestsCount,
      icon: UserPlus,
      color: 'text-orange-500',
      bgColor: 'bg-orange-50',
      onClick: () => navigate('/connections'),
    },
    {
      title: 'Profile Views',
      value: stats.profileViews,
      icon: TrendingUp,
      color: 'text-purple-500',
      bgColor: 'bg-purple-50',
      onClick: () => navigate('/profile'),
    },
  ];

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Welcome Section */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-light-text-primary mb-2">
          Welcome back!
        </h1>
        <p className="text-light-text-secondary">
          Here's what's happening with your CollegeBuddy account
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {statCards.map((card) => {
          const Icon = card.icon;
          return (
            <button
              key={card.title}
              onClick={card.onClick}
              className="card p-6 hover:shadow-md transition-shadow cursor-pointer text-left"
            >
              <div className="flex items-start justify-between mb-4">
                <div className={clsx('p-3 rounded-xl', card.bgColor)}>
                  <Icon className={clsx('w-6 h-6', card.color)} />
                </div>
              </div>
              <h3 className="text-2xl font-bold text-light-text-primary mb-1">
                {card.value}
              </h3>
              <p className="text-sm text-light-text-secondary">
                {card.title}
              </p>
            </button>
          );
        })}
      </div>

      {/* Connections List */}
      {friends.length > 0 && (
        <div className="card p-6 mb-8">
          <h2 className="text-xl font-bold text-light-text-primary mb-4">
            Your Connections ({friends.length})
          </h2>
          <div className="space-y-3">
            {friends.map((friend) => (
              <div
                key={friend.userId}
                className="flex items-center justify-between p-4 bg-light-surface rounded-lg"
              >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <Avatar
                    src={friend.avatarUrl}
                    alt={friend.displayName}
                    size="md"
                    fallback={friend.displayName}
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-light-text-primary truncate">
                      {friend.displayName}
                    </p>
                    <p className="text-sm text-light-text-secondary">
                      User ID: {friend.userId}
                    </p>
                  </div>
                </div>
                <div className="flex gap-2 flex-shrink-0">
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => navigate(`/chat/${friend.userId}`)}
                    className="gap-2"
                  >
                    <MessageCircle className="w-4 h-4" />
                    Message
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() =>
                      setConfirmDisconnect({
                        userId: friend.userId,
                        displayName: friend.displayName,
                      })
                    }
                    className="gap-2"
                  >
                    <UserMinus className="w-4 h-4" />
                    Disconnect
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Pending Connection Requests */}
      {incomingRequests.length > 0 && (
        <div className="card p-6 mb-8">
          <h2 className="text-xl font-bold text-light-text-primary mb-4">
            Pending Connection Requests
          </h2>
          <div className="space-y-4">
            {incomingRequests.slice(0, 3).map((request) => (
              <div
                key={request.requesterId}
                className="flex items-center justify-between p-4 bg-light-surface rounded-lg"
              >
                <div className="flex items-center gap-3">
                  <Avatar
                    src={request.requesterAvatar}
                    alt={request.requesterName}
                    size="md"
                    fallback={request.requesterName}
                  />
                  <div>
                    <p className="font-semibold text-light-text-primary">
                      {request.requesterName}
                    </p>
                    <p className="text-sm text-light-text-secondary">
                      wants to connect
                    </p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => handleAcceptRequest(request.requesterId)}
                  >
                    Accept
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => handleDeclineRequest(request.requesterId)}
                  >
                    Decline
                  </Button>
                </div>
              </div>
            ))}
            {incomingRequests.length > 3 && (
              <button
                onClick={() => navigate('/connections')}
                className="text-sm text-blue-500 hover:text-blue-600 font-semibold"
              >
                View all {incomingRequests.length} requests
              </button>
            )}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="card p-6">
        <h2 className="text-xl font-bold text-light-text-primary mb-4">
          Quick Actions
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Button
            variant="secondary"
            onClick={() => navigate('/search')}
            className="justify-start"
          >
            <Users className="w-5 h-5 mr-2" />
            Find Classmates
          </Button>
          <Button
            variant="secondary"
            onClick={() => navigate('/messages')}
            className="justify-start"
          >
            <MessageCircle className="w-5 h-5 mr-2" />
            View Messages
          </Button>
          <Button
            variant="secondary"
            onClick={() => navigate('/profile')}
            className="justify-start"
          >
            <Users className="w-5 h-5 mr-2" />
            Edit Profile
          </Button>
          <Button
            variant="secondary"
            onClick={() => navigate('/connections')}
            className="justify-start"
          >
            <UserPlus className="w-5 h-5 mr-2" />
            Manage Connections
          </Button>
        </div>
      </div>

      {/* Disconnect Confirmation Modal */}
      <Modal
        isOpen={!!confirmDisconnect}
        onClose={() => setConfirmDisconnect(null)}
        title="Disconnect from connection"
      >
        <div className="space-y-4">
          <p className="text-light-text-secondary">
            Are you sure you want to disconnect from{' '}
            <strong className="text-light-text-primary">
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
    </div>
  );
}
