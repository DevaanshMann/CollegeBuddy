import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Users, Globe, Lock, LogOut, Shield, ArrowLeft, Crown, MessageCircle } from 'lucide-react';
import { groupsApi } from '../../api/groups';
import type { GroupDto, GroupMemberDto } from '../../api/groups';
import { Avatar, Button, Modal } from '../../components/ui';
import toast from 'react-hot-toast';

export function GroupDetailPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const [group, setGroup] = useState<GroupDto | null>(null);
  const [members, setMembers] = useState<GroupMemberDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [leaving, setLeaving] = useState(false);

  useEffect(() => {
    if (groupId) {
      loadGroupData();
    }
  }, [groupId]);

  async function loadGroupData() {
    setLoading(true);
    try {
      const [groupData, membersData] = await Promise.all([
        groupsApi.getGroupDetails(Number(groupId)),
        groupsApi.getGroupMembers(Number(groupId)),
      ]);

      setGroup(groupData);
      setMembers(membersData);
    } catch (err: any) {
      console.error('Failed to load group data:', err);
      toast.error(err.message ?? 'Failed to load group');
    } finally {
      setLoading(false);
    }
  }

  async function handleJoinGroup() {
    if (!groupId) return;

    try {
      await groupsApi.joinGroup(Number(groupId));
      toast.success('Joined group successfully!');
      loadGroupData();
    } catch (err: any) {
      console.error('Join group error:', err);
      toast.error(err.message ?? 'Failed to join group');
    }
  }

  async function handleLeaveGroup() {
    if (!groupId) return;

    setLeaving(true);
    try {
      await groupsApi.leaveGroup(Number(groupId));
      toast.success('Left group successfully');
      navigate('/groups');
    } catch (err: any) {
      console.error('Leave group error:', err);
      toast.error(err.message ?? 'Failed to leave group');
      setShowLeaveModal(false);
    } finally {
      setLeaving(false);
    }
  }

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8">
        <div className="text-center py-12">
          <div className="inline-block w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="mt-4 text-light-text-secondary dark:text-dark-text-secondary">
            Loading group...
          </p>
        </div>
      </div>
    );
  }

  if (!group) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8">
        <div className="text-center py-12">
          <h3 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
            Group not found
          </h3>
          <Button variant="primary" onClick={() => navigate('/groups')}>
            Back to Groups
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Back Button */}
      <button
        onClick={() => navigate('/groups')}
        className="flex items-center gap-2 text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 mb-6 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Groups
      </button>

      {/* Group Header */}
      <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6 mb-6">
        <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
          <div className="flex-1">
            <div className="flex items-start gap-3 mb-3">
              <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center flex-shrink-0">
                <Users className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="flex-1 min-w-0">
                <h1 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-1">
                  {group.name}
                </h1>
                <div className="flex items-center gap-3 text-sm text-light-text-secondary dark:text-dark-text-secondary">
                  <span>Created by {group.creatorName}</span>
                  <span>•</span>
                  <div className="flex items-center gap-1">
                    {group.visibility === 'PUBLIC' ? (
                      <>
                        <Globe className="w-4 h-4 text-green-500" />
                        <span>Public</span>
                      </>
                    ) : (
                      <>
                        <Lock className="w-4 h-4 text-gray-500" />
                        <span>Private</span>
                      </>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {group.description && (
              <p className="text-light-text-secondary dark:text-dark-text-secondary">
                {group.description}
              </p>
            )}

            <div className="mt-4 flex items-center gap-1 text-sm text-light-text-secondary dark:text-dark-text-secondary">
              <Users className="w-4 h-4" />
              <span>
                {group.memberCount} {group.memberCount === 1 ? 'member' : 'members'}
              </span>
            </div>
          </div>

          <div className="flex-shrink-0 flex gap-3">
            {group.isMember && (
              <Button
                variant="primary"
                onClick={() => navigate(`/groups/${groupId}/chat`)}
                className="gap-2"
              >
                <MessageCircle className="w-4 h-4" />
                Group Chat
              </Button>
            )}
            {group.isMember ? (
              <Button
                variant="secondary"
                onClick={() => setShowLeaveModal(true)}
                className="gap-2"
              >
                <LogOut className="w-4 h-4" />
                Leave Group
              </Button>
            ) : (
              <Button
                variant="primary"
                onClick={handleJoinGroup}
                className="gap-2"
              >
                <Users className="w-4 h-4" />
                Join Group
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Members Section */}
      <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
        <h2 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-4">
          Members ({members.length})
        </h2>

        <div className="space-y-3">
          {members.map((member) => (
            <div
              key={member.userId}
              className="flex items-center justify-between p-3 hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-lg transition-colors"
            >
              <div className="flex items-center gap-3 flex-1 min-w-0">
                <Avatar
                  src={member.avatarUrl}
                  alt={member.displayName}
                  size="md"
                  fallback={member.displayName}
                />
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-light-text-primary dark:text-dark-text-primary truncate">
                    {member.displayName}
                  </p>
                  <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                    Joined {new Date(member.joinedAt).toLocaleDateString()}
                  </p>
                </div>
              </div>

              {member.role === 'ADMIN' && (
                <div className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-semibold rounded-full dark:bg-blue-300 dark:text-black">
                  {member.userId === group.creatorId ? (
                    <>
                      <Crown className="w-3 h-3" />
                      Creator
                    </>
                  ) : (
                    <>
                      <Shield className="w-3 h-3" />
                      Admin
                    </>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Leave Group Modal */}
      <Modal
        isOpen={showLeaveModal}
        onClose={() => setShowLeaveModal(false)}
        title="Leave Group"
      >
        <div className="space-y-4">
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Are you sure you want to leave{' '}
            <strong className="text-light-text-primary dark:text-dark-text-primary">
              {group.name}
            </strong>
            ? You can rejoin later if you change your mind.
          </p>

          {group.isAdmin && (
            <div className="p-3 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
              <p className="text-sm text-yellow-800 dark:text-yellow-200">
                <strong>Note:</strong> You are an admin of this group. Make sure to transfer admin
                rights to another member if needed before leaving.
              </p>
            </div>
          )}

          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => setShowLeaveModal(false)}
              disabled={leaving}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              fullWidth
              onClick={handleLeaveGroup}
              loading={leaving}
              className="gap-2"
            >
              <LogOut className="w-4 h-4" />
              Leave Group
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
