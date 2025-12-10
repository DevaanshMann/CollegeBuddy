import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trash2, AlertTriangle, Lock } from 'lucide-react';
import { accountApi } from '../../api/account';
import { Button, Modal } from '../../components/ui';
import { useAuth } from '../../contexts/AuthContext';
import toast from 'react-hot-toast';

export function DeleteAccountPage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleDeleteAccount() {
    if (!password.trim()) {
      toast.error('Please enter your password');
      return;
    }

    setLoading(true);

    try {
      await accountApi.deleteAccount(password);
      toast.success('Account deleted successfully');

      // Log out and redirect to landing page
      logout();
      navigate('/', { replace: true });
    } catch (err: any) {
      console.error('Delete account error:', err);
      toast.error(err.message ?? 'Failed to delete account');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2 flex items-center gap-2">
          <Trash2 className="w-6 h-6 text-red-500" />
          Delete Account
        </h1>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Permanently delete your account and all associated data
        </p>
      </div>

      {/* Warning Card */}
      <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6 mb-6">
        <div className="flex gap-3">
          <AlertTriangle className="w-6 h-6 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
          <div>
            <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
              <span className="text-red-600 dark:text-red-400">WARNING:</span>{' '}
              THIS ACTION CANNOT BE UNDONE
            </h3>
            <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-3">
              Deleting your account will permanently remove:
            </p>
            <ul className="list-disc list-inside text-sm text-light-text-secondary dark:text-dark-text-secondary space-y-1 ml-2">
              <li>Your profile and personal information</li>
              <li>All your connections and friend requests</li>
              <li>All your messages and conversations</li>
              <li>Your avatar and uploaded files</li>
              <li>Your search history and preferences</li>
            </ul>
            <p className="text-sm text-light-text-primary dark:text-dark-text-primary mt-3 font-semibold">
              This data cannot be recovered once deleted.
            </p>
          </div>
        </div>
      </div>

      {/* Before You Go Section */}
      <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6 mb-6">
        <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-3">
          Before you go...
        </h3>
        <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-3">
          If you're experiencing issues with CollegeBuddy, we're here to help! Consider these alternatives:
        </p>
        <ul className="space-y-2 text-sm text-light-text-secondary dark:text-dark-text-secondary">
          <li className="flex items-start gap-2">
            <span className="text-blue-500 mt-0.5">•</span>
            <span>Having privacy concerns? You can block specific users or adjust your profile visibility settings.</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-500 mt-0.5">•</span>
            <span>Need a break? Simply log out and come back whenever you're ready.</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="text-blue-500 mt-0.5">•</span>
            <span>Found a bug? Contact support so we can fix it for everyone.</span>
          </li>
        </ul>
      </div>

      {/* Delete Button */}
      <Button
        variant="danger"
        onClick={() => setShowConfirmModal(true)}
        className="gap-2"
        fullWidth
      >
        <Trash2 className="w-4 h-4" />
        Delete My Account
      </Button>

      {/* Confirmation Modal */}
      <Modal
        isOpen={showConfirmModal}
        onClose={() => {
          setShowConfirmModal(false);
          setPassword('');
        }}
        title="Confirm Account Deletion"
      >
        <div className="space-y-4">
          <div className="flex items-start gap-3 p-4 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg">
            <AlertTriangle className="w-6 h-6 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <div className="text-sm">
              <p className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-1">
                <span className="text-red-600 dark:text-red-400">WARNING:</span> This action is permanent and irreversible
              </p>
              <p className="text-light-text-secondary dark:text-dark-text-secondary">
                All your data will be permanently deleted from our servers.
              </p>
            </div>
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-light-text-primary dark:text-dark-text-primary mb-2"
            >
              Enter your password to confirm
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-500 dark:text-gray-400" />
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleDeleteAccount()}
                placeholder="Enter your password"
                className="w-full pl-10 pr-4 py-2.5 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 text-light-text-primary dark:text-dark-text-primary"
                autoFocus
              />
            </div>
          </div>

          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => {
                setShowConfirmModal(false);
                setPassword('');
              }}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              fullWidth
              onClick={handleDeleteAccount}
              loading={loading}
              className="gap-2"
            >
              <Trash2 className="w-4 h-4" />
              Delete Account
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
