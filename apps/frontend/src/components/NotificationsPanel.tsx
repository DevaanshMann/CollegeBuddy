import { useEffect } from 'react';
import { X, UserPlus, UserCheck, MessageSquare, Users } from 'lucide-react';
import { Button } from './ui';
import type { NotificationDto } from '../types';
import { clsx } from 'clsx';
import { motion, AnimatePresence } from 'framer-motion';

interface NotificationsPanelProps {
  isOpen: boolean;
  onClose: () => void;
  notifications: NotificationDto[];
  onAcceptRequest?: (userId: number) => void;
  onDeclineRequest?: (userId: number) => void;
  onMarkAllRead?: () => void;
  onNotificationClick?: (notification: NotificationDto) => void;
}

export function NotificationsPanel({
  isOpen,
  onClose,
  notifications,
  onAcceptRequest,
  onDeclineRequest,
  onMarkAllRead,
  onNotificationClick
}: NotificationsPanelProps) {
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, onClose]);

  const getNotificationIcon = (type: NotificationDto['type']) => {
    switch (type) {
      case 'CONNECTION_REQUEST':
        return <UserPlus className="w-5 h-5 text-primary-500" />;
      case 'CONNECTION_ACCEPTED':
        return <UserCheck className="w-5 h-5 text-green-500" />;
      case 'NEW_MESSAGE':
        return <MessageSquare className="w-5 h-5 text-blue-500" />;
      case 'NEW_GROUP_MESSAGE':
        return <Users className="w-5 h-5 text-purple-500" />;
      default:
        return null;
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInMins = Math.floor(diffInMs / 60000);
    const diffInHours = Math.floor(diffInMs / 3600000);
    const diffInDays = Math.floor(diffInMs / 86400000);

    if (diffInMins < 1) return 'Just now';
    if (diffInMins < 60) return `${diffInMins}m ago`;
    if (diffInHours < 24) return `${diffInHours}h ago`;
    if (diffInDays < 7) return `${diffInDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <>
      {/* Backdrop */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 backdrop-blur-light z-40"
            onClick={onClose}
          />
        )}
      </AnimatePresence>

      {/* Panel */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="fixed right-0 top-0 h-full w-full max-w-md bg-white dark:bg-dark-bg border-l border-light-border dark:border-dark-border z-50 flex flex-col shadow-2xl"
          >
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-light-border dark:border-dark-border">
              <h2 className="text-xl font-bold text-light-text-primary dark:text-dark-text-primary">
                Notifications
              </h2>
              <button
                onClick={onClose}
                className="p-2 hover:bg-gray-100 dark:hover:bg-dark-surface rounded-full transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Mark all read */}
            {notifications.some(n => !n.isRead) && (
              <div className="px-4 py-2 border-b border-light-border dark:border-dark-border">
                <button
                  onClick={onMarkAllRead}
                  className="text-sm text-primary-500 hover:text-primary-600 font-semibold"
                >
                  Mark all as read
                </button>
              </div>
            )}

            {/* Notifications List */}
            <div className="flex-1 overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full p-8 text-center">
                  <div className="w-16 h-16 bg-gray-100 dark:bg-dark-surface rounded-full flex items-center justify-center mb-4">
                    <X className="w-8 h-8 text-gray-500 dark:text-gray-400" />
                  </div>
                  <h3 className="text-lg font-semibold mb-2 text-light-text-primary dark:text-dark-text-primary">
                    No notifications
                  </h3>
                  <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                    When you get notifications, they'll show up here
                  </p>
                </div>
              ) : (
                <div className="divide-y divide-light-border dark:divide-dark-border">
                  {notifications.map((notification) => (
                    <div
                      key={notification.id}
                      onClick={() => {
                        if (notification.type === 'NEW_MESSAGE' || notification.type === 'NEW_GROUP_MESSAGE') {
                          onNotificationClick?.(notification);
                        }
                      }}
                      className={clsx(
                        'p-4 hover:bg-gray-50 dark:hover:bg-dark-surface transition-colors',
                        !notification.isRead && 'bg-blue-50 dark:bg-blue-900/10',
                        (notification.type === 'NEW_MESSAGE' || notification.type === 'NEW_GROUP_MESSAGE') && 'cursor-pointer'
                      )}
                    >
                      <div className="flex gap-3">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-start gap-2 mb-1">
                            {getNotificationIcon(notification.type)}
                            <div className="flex-1">
                              <p className="text-sm text-light-text-primary dark:text-dark-text-primary">
                                {notification.userName && (
                                  <>
                                    <span className="font-semibold">{notification.userName}</span>
                                    {' '}
                                  </>
                                )}
                                {notification.message}
                              </p>
                              <p className="text-xs text-light-text-secondary dark:text-dark-text-secondary mt-1">
                                {formatTimestamp(notification.timestamp)}
                              </p>
                            </div>
                          </div>

                          {/* Action buttons for connection requests */}
                          {notification.type === 'CONNECTION_REQUEST' && notification.userId && (
                            <div className="flex gap-2 mt-3">
                              <Button
                                variant="primary"
                                size="sm"
                                onClick={() => onAcceptRequest?.(notification.userId!)}
                              >
                                Accept
                              </Button>
                              <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => onDeclineRequest?.(notification.userId!)}
                              >
                                Decline
                              </Button>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
