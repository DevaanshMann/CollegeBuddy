import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, Plus, Search, Globe, Lock, ChevronLeft, ChevronRight, MessageSquare } from 'lucide-react';
import { groupsApi } from '../../api/groups';
import type { GroupDto, CreateGroupRequest, Visibility } from '../../api/groups';
import { Button, Modal } from '../../components/ui';
import toast from 'react-hot-toast';

export function GroupsPage() {
  const navigate = useNavigate();
  const [groups, setGroups] = useState<GroupDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [creating, setCreating] = useState(false);

  // Create group form state
  const [groupName, setGroupName] = useState('');
  const [groupDescription, setGroupDescription] = useState('');
  const [groupVisibility, setGroupVisibility] = useState<Visibility>('PUBLIC');

  useEffect(() => {
    loadGroups();
  }, [page, searchQuery]);

  async function loadGroups() {
    setLoading(true);
    try {
      const response = await groupsApi.getGroups(page, 12, searchQuery || undefined);
      setGroups(response.content);
      setTotalPages(response.totalPages);
    } catch (err: any) {
      console.error('Failed to load groups:', err);
      toast.error(err.message ?? 'Failed to load groups');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateGroup() {
    if (!groupName.trim()) {
      toast.error('Group name is required');
      return;
    }

    setCreating(true);
    try {
      const request: CreateGroupRequest = {
        name: groupName.trim(),
        description: groupDescription.trim() || undefined,
        visibility: groupVisibility,
      };

      await groupsApi.createGroup(request);
      toast.success('Group created successfully!');

      // Reset form
      setGroupName('');
      setGroupDescription('');
      setGroupVisibility('PUBLIC');
      setShowCreateModal(false);

      // Reload groups
      loadGroups();
    } catch (err: any) {
      console.error('Create group error:', err);
      toast.error(err.message ?? 'Failed to create group');
    } finally {
      setCreating(false);
    }
  }

  async function handleJoinGroup(groupId: number) {
    try {
      await groupsApi.joinGroup(groupId);
      toast.success('Joined group successfully!');
      loadGroups();
    } catch (err: any) {
      console.error('Join group error:', err);
      toast.error(err.message ?? 'Failed to join group');
    }
  }

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    setPage(0);
    // searchQuery state change will trigger useEffect to reload
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2">
          Campus Groups
        </h1>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Join or create groups to connect with classmates
        </p>
      </div>

      {/* Search and Create */}
      <div className="flex flex-col sm:flex-row gap-4 mb-6">
        <form onSubmit={handleSearch} className="flex-1">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-500 dark:text-gray-400" />
            <input
              type="text"
              placeholder="Search groups..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-light-text-primary dark:text-dark-text-primary"
            />
          </div>
        </form>

        <Button
          variant="primary"
          onClick={() => setShowCreateModal(true)}
          className="gap-2"
        >
          <Plus className="w-4 h-4" />
          Create Group
        </Button>
      </div>

      {/* Groups Grid */}
      {loading ? (
        <div className="text-center py-12">
          <div className="inline-block w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="mt-4 text-light-text-secondary dark:text-dark-text-secondary">
            Loading groups...
          </p>
        </div>
      ) : groups.length === 0 ? (
        <div className="text-center py-16">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
            <Users className="w-8 h-8 text-gray-500 dark:text-gray-400" />
          </div>
          <h3 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
            {searchQuery ? 'No groups found' : 'No groups yet'}
          </h3>
          <p className="text-light-text-secondary dark:text-dark-text-secondary max-w-md mx-auto mb-6">
            {searchQuery
              ? 'Try a different search term'
              : 'Be the first to create a group for your campus!'}
          </p>
          {!searchQuery && (
            <Button variant="primary" onClick={() => setShowCreateModal(true)} className="gap-2">
              <Plus className="w-4 h-4" />
              Create First Group
            </Button>
          )}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {groups.map((group) => (
              <div
                key={group.id}
                className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-5 hover:border-blue-500 dark:hover:border-blue-500 transition-all cursor-pointer relative"
                onClick={() => navigate(`/groups/${group.id}`)}
              >
                {/* Unread Badge */}
                {group.isMember && group.unreadCount > 0 && (
                  <div className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold rounded-full w-6 h-6 flex items-center justify-center shadow-lg">
                    {group.unreadCount > 9 ? '9+' : group.unreadCount}
                  </div>
                )}

                {/* Group Header */}
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary text-lg truncate">
                        {group.name}
                      </h3>
                      {group.isMember && group.unreadCount > 0 && (
                        <MessageSquare className="w-4 h-4 text-red-500 flex-shrink-0" />
                      )}
                    </div>
                    <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary truncate">
                      by {group.creatorName}
                    </p>
                  </div>
                  <div className="flex-shrink-0 ml-2">
                    {group.visibility === 'PUBLIC' ? (
                      <Globe className="w-5 h-5 text-green-500" />
                    ) : (
                      <Lock className="w-5 h-5 text-gray-500" />
                    )}
                  </div>
                </div>

                {/* Description */}
                {group.description && (
                  <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-4 line-clamp-2">
                    {group.description}
                  </p>
                )}

                {/* Footer */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1 text-sm text-light-text-secondary dark:text-dark-text-secondary">
                    <Users className="w-4 h-4" />
                    <span>{group.memberCount} {group.memberCount === 1 ? 'member' : 'members'}</span>
                  </div>

                  {group.isMember ? (
                    <span className="text-sm font-semibold text-blue-600 dark:text-blue-400">
                      Joined
                    </span>
                  ) : (
                    <div onClick={(e) => e.stopPropagation()}>
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => handleJoinGroup(group.id)}
                      >
                        Join
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-8">
              <Button
                variant="secondary"
                size="sm"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="gap-1"
              >
                <ChevronLeft className="w-4 h-4" />
                Previous
              </Button>
              <span className="text-sm text-light-text-secondary dark:text-dark-text-secondary px-4">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="secondary"
                size="sm"
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="gap-1"
              >
                Next
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          )}
        </>
      )}

      {/* Create Group Modal */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => {
          setShowCreateModal(false);
          setGroupName('');
          setGroupDescription('');
          setGroupVisibility('PUBLIC');
        }}
        title="Create New Group"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-light-text-primary dark:text-dark-text-primary mb-2">
              Group Name *
            </label>
            <input
              type="text"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
              placeholder="e.g., CS 5800 Study Group"
              maxLength={100}
              className="w-full px-3 py-2 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-light-text-primary dark:text-dark-text-primary"
              autoFocus
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-light-text-primary dark:text-dark-text-primary mb-2">
              Description (optional)
            </label>
            <textarea
              value={groupDescription}
              onChange={(e) => setGroupDescription(e.target.value)}
              placeholder="What is this group about?"
              maxLength={500}
              rows={3}
              className="w-full px-3 py-2 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-light-text-primary dark:text-dark-text-primary resize-none"
            />
            <p className="text-xs text-light-text-secondary dark:text-dark-text-secondary mt-1">
              {groupDescription.length}/500
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-light-text-primary dark:text-dark-text-primary mb-2">
              Visibility
            </label>
            <div className="space-y-2">
              <label className="flex items-start gap-3 p-3 border border-light-border dark:border-dark-border rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                <input
                  type="radio"
                  name="visibility"
                  value="PUBLIC"
                  checked={groupVisibility === 'PUBLIC'}
                  onChange={() => setGroupVisibility('PUBLIC')}
                  className="mt-1"
                />
                <div>
                  <div className="flex items-center gap-2">
                    <Globe className="w-4 h-4 text-green-500" />
                    <span className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                      Public
                    </span>
                  </div>
                  <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mt-1">
                    Anyone on your campus can find and join this group
                  </p>
                </div>
              </label>

              <label className="flex items-start gap-3 p-3 border border-light-border dark:border-dark-border rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                <input
                  type="radio"
                  name="visibility"
                  value="PRIVATE"
                  checked={groupVisibility === 'PRIVATE'}
                  onChange={() => setGroupVisibility('PRIVATE')}
                  className="mt-1"
                />
                <div>
                  <div className="flex items-center gap-2">
                    <Lock className="w-4 h-4 text-gray-500" />
                    <span className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                      Private
                    </span>
                  </div>
                  <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mt-1">
                    Only invited members can join (coming soon)
                  </p>
                </div>
              </label>
            </div>
          </div>

          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => {
                setShowCreateModal(false);
                setGroupName('');
                setGroupDescription('');
                setGroupVisibility('PUBLIC');
              }}
              disabled={creating}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              fullWidth
              onClick={handleCreateGroup}
              loading={creating}
              className="gap-2"
            >
              <Plus className="w-4 h-4" />
              Create Group
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
