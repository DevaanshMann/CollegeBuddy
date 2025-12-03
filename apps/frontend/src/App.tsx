import { BrowserRouter, Routes, Route, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Toaster } from 'react-hot-toast';
import { ThemeProvider } from './contexts/ThemeContext';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { Sidebar } from './components/Sidebar';
import { NotificationsPanel } from './components/NotificationsPanel';
import { LandingPage } from './pages/Landing/LandingPage';
import { SignupPage } from './pages/Auth/SignupPage';
import { LoginPage } from './pages/Auth/LoginPage';
import { VerifyPage } from './pages/Auth/VerifyPage';
import { HomePage } from './pages/Home/HomePage';
import { ProfilePage } from './pages/Profile/ProfilePage';
import { SearchPage } from './pages/Search/SearchPage';
import { ConnectionsPage } from './pages/Connections/ConnectionsPage';
import { ChatPage } from './pages/Chat/ChatPage';
import { SettingsPage } from './pages/Settings/SettingsPage';
import { BlockedUsersPage } from './pages/Settings/BlockedUsersPage';
import { DeleteAccountPage } from './pages/Settings/DeleteAccountPage';
import { AdminDashboardPage } from './pages/Admin/AdminDashboardPage';
import { GroupsPage } from './pages/Groups/GroupsPage';
import { GroupDetailPage } from './pages/Groups/GroupDetailPage';
import { GroupChatPage } from './pages/Groups/GroupChatPage';
import type { NotificationDto } from './types';
import { apiClient } from './api/client';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/home" replace />;
  }

  return <>{children}</>;
}

function AppContent() {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const [unreadMessages, setUnreadMessages] = useState(0);

  // Public routes (no sidebar)
  const publicRoutes = ['/', '/login', '/signup', '/verify'];
  const isPublicRoute = publicRoutes.includes(location.pathname);

  useEffect(() => {
    if (isAuthenticated) {
      loadNotifications();
      loadUnreadMessages();
    }
  }, [isAuthenticated, location.pathname]);

  // Listen for messages being read to update notifications immediately
  useEffect(() => {
    const handleMessagesRead = () => {
      loadNotifications();
      loadUnreadMessages();
    };

    window.addEventListener('messagesRead', handleMessagesRead);
    return () => window.removeEventListener('messagesRead', handleMessagesRead);
  }, []);

  const loadNotifications = async () => {
    try {
      const [connectionsData, conversationsData] = await Promise.all([
        apiClient.get<{
          incomingRequests: any[];
        }>('/connections'),
        apiClient.get<any[]>('/messages/conversations')
      ]);

      // Convert connection requests to notifications
      const requestNotifications: NotificationDto[] = connectionsData.incomingRequests.map(
        (req: any) => ({
          id: `req-${req.requesterId}`,
          type: 'CONNECTION_REQUEST' as const,
          userId: req.requesterId,
          userName: req.requesterName,
          userAvatar: req.requesterAvatar,
          message: 'sent you a connection request',
          timestamp: req.createdAt || new Date().toISOString(),
          isRead: false,
        })
      );

      // Convert conversations with unread messages to notifications
      const messageNotifications: NotificationDto[] = conversationsData
        .filter((conv: any) => conv.unreadCount > 0)
        .map((conv: any) => ({
          id: `msg-${conv.otherUserId}`,
          type: 'NEW_MESSAGE' as const,
          userId: conv.otherUserId,
          userName: conv.otherUserName,
          userAvatar: conv.otherUserAvatar,
          message: `sent you ${conv.unreadCount} new message${conv.unreadCount > 1 ? 's' : ''}`,
          timestamp: conv.lastMessageTime || new Date().toISOString(),
          isRead: false,
        }));

      // Combine and sort by timestamp (most recent first)
      const allNotifications = [...requestNotifications, ...messageNotifications].sort(
        (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
      );

      setNotifications(allNotifications);
    } catch (error) {
      console.error('Failed to load notifications:', error);
    }
  };

  const loadUnreadMessages = async () => {
    try {
      const connectionsData = await apiClient.get<{
        connections: any[];
        unreadCounts: Record<number, number>;
      }>('/connections');

      // Count number of people with unread messages, not total messages
      const unreadCount = Object.values(connectionsData.unreadCounts || {}).filter(
        (count: number) => count > 0
      ).length;

      setUnreadMessages(unreadCount);
    } catch (error) {
      console.error('Failed to load unread messages:', error);
    }
  };

  const handleAcceptRequest = async (userId: number) => {
    try {
      await apiClient.post('/connections/respond', {
        otherUserId: userId,
        accept: true,
      });
      loadNotifications(); // Refresh notifications
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
      loadNotifications(); // Refresh notifications
    } catch (error) {
      console.error('Failed to decline request:', error);
    }
  };

  const handleNotificationClick = (notification: NotificationDto) => {
    if (notification.type === 'NEW_MESSAGE') {
      navigate(`/chat/${notification.userId}`);
      setShowNotifications(false);
    }
  };

  return (
    <div className="min-h-screen bg-light-bg dark:bg-dark-bg">
      {/* Sidebar for authenticated users on non-public routes */}
      {isAuthenticated && !isPublicRoute && (
        <Sidebar
          onNotificationsClick={() => setShowNotifications(true)}
          unreadNotifications={notifications.filter(n => !n.isRead).length}
          unreadMessages={unreadMessages}
        />
      )}

      {/* Notifications Panel */}
      <NotificationsPanel
        isOpen={showNotifications}
        onClose={() => setShowNotifications(false)}
        notifications={notifications}
        onAcceptRequest={handleAcceptRequest}
        onDeclineRequest={handleDeclineRequest}
        onMarkAllRead={() => {
          setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
        }}
        onNotificationClick={handleNotificationClick}
      />

      {/* Main Content */}
      <main
        className={`
          ${isAuthenticated && !isPublicRoute ? 'ml-64' : ''}
          min-h-screen
        `}
      >
        <Routes>
          {/* Public routes */}
          <Route
            path="/"
            element={isAuthenticated ? <Navigate to="/home" replace /> : <LandingPage />}
          />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/verify" element={<VerifyPage />} />

          {/* Protected routes */}
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <HomePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/search"
            element={
              <ProtectedRoute>
                <SearchPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/connections"
            element={
              <ProtectedRoute>
                <ConnectionsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/messages"
            element={
              <ProtectedRoute>
                <ChatPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/chat/:otherUserId"
            element={
              <ProtectedRoute>
                <ChatPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <SettingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings/blocked-users"
            element={
              <ProtectedRoute>
                <BlockedUsersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings/delete-account"
            element={
              <ProtectedRoute>
                <DeleteAccountPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminDashboardPage />
              </AdminRoute>
            }
          />
          <Route
            path="/groups"
            element={
              <ProtectedRoute>
                <GroupsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/groups/:groupId"
            element={
              <ProtectedRoute>
                <GroupDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/groups/:groupId/chat"
            element={
              <ProtectedRoute>
                <GroupChatPage />
              </ProtectedRoute>
            }
          />

          {/* Catch all - redirect to home or landing */}
          <Route
            path="*"
            element={<Navigate to={isAuthenticated ? '/home' : '/'} replace />}
          />
        </Routes>
      </main>

      {/* Toast notifications */}
      <Toaster
        position="bottom-center"
        toastOptions={{
          duration: 4000,
          style: {
            background: 'rgb(38, 38, 38)',
            color: 'rgb(255, 255, 255)',
            padding: '16px',
            borderRadius: '8px',
            fontSize: '14px',
            fontWeight: '500',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
            border: '1px solid rgb(54, 54, 54)',
          },
          success: {
            style: {
              background: 'rgb(34, 197, 94)',
              color: 'rgb(255, 255, 255)',
            },
            iconTheme: {
              primary: 'rgb(255, 255, 255)',
              secondary: 'rgb(34, 197, 94)',
            },
          },
          error: {
            style: {
              background: 'rgb(239, 68, 68)',
              color: 'rgb(255, 255, 255)',
            },
            iconTheme: {
              primary: 'rgb(255, 255, 255)',
              secondary: 'rgb(239, 68, 68)',
            },
          },
        }}
      />
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <AppContent />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}

export default App;
