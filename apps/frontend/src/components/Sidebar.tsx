import { useLocation, useNavigate } from 'react-router-dom';
import {
  Home,
  Search,
  MessageCircle,
  User,
  Bell,
  Menu,
  Moon,
  Sun,
  LogOut,
  Settings as SettingsIcon,
  Users,
  Shield
} from 'lucide-react';
import { Avatar, Badge } from './ui';
import { useAuth } from '../contexts/AuthContext';
import { useTheme } from '../contexts/ThemeContext';
import { useState } from 'react';
import { clsx } from 'clsx';

interface SidebarProps {
  onNotificationsClick: () => void;
  unreadNotifications?: number;
  unreadMessages?: number;
  unreadGroups?: number;
}

export function Sidebar({
  onNotificationsClick,
  unreadNotifications = 0,
  unreadMessages = 0,
  unreadGroups = 0
}: SidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [showMenu, setShowMenu] = useState(false);

  const navigationItems = [
    { icon: Home, label: 'Home', path: '/home' },
    { icon: Search, label: 'Search', path: '/search' },
    { icon: Users, label: 'Groups', path: '/groups', badge: unreadGroups },
    { icon: MessageCircle, label: 'Messages', path: '/messages', badge: unreadMessages },
    { icon: User, label: 'Profile', path: '/profile' },
  ];

  // Add Admin link if user is an admin
  if (user?.role === 'ADMIN') {
    navigationItems.push({ icon: Shield, label: 'Admin', path: '/admin' });
  }

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  return (
    <aside className="fixed left-0 top-0 h-full w-64 bg-light-bg border-r border-light-border flex flex-col z-40">
      {/* Logo */}
      <div className="p-6 border-b border-light-border">
        <h1 className="text-2xl font-bold text-blue-500" style={{ fontFamily: "'Pacifico', cursive" }}>
          CollegeBuddy
        </h1>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1">
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.path);

          return (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className={clsx(
                'nav-link w-full',
                active && 'nav-link-active'
              )}
            >
              <div className="relative">
                <Icon
                  className={clsx(
                    'w-6 h-6 text-light-text-primary dark:text-dark-text-primary',
                    active ? 'stroke-[2.5]' : 'stroke-2'
                  )}
                />
                {item.badge !== undefined && item.badge > 0 && (
                  <div className="absolute -top-1 -right-1">
                    <Badge count={item.badge} />
                  </div>
                )}
              </div>
              <span className={clsx(
                'text-light-text-primary dark:text-dark-text-primary',
                active && 'font-bold'
              )}>
                {item.label}
              </span>
            </button>
          );
        })}

        {/* Notifications */}
        <button
          onClick={onNotificationsClick}
          className="nav-link w-full"
        >
          <div className="relative">
            <Bell className="w-6 h-6 text-light-text-primary dark:text-dark-text-primary" />
            {unreadNotifications > 0 && (
              <div className="absolute -top-1 -right-1">
                <Badge count={unreadNotifications} />
              </div>
            )}
          </div>
          <span className="text-light-text-primary dark:text-dark-text-primary">Notifications</span>
        </button>
      </nav>

      {/* Bottom Menu */}
      <div className="p-4 border-t border-light-border space-y-2">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="nav-link w-full"
        >
          {theme === 'light' ? (
            <>
              <Moon className="w-6 h-6 text-light-text-primary dark:text-dark-text-primary" />
              <span className="text-light-text-primary dark:text-dark-text-primary">Dark Mode</span>
            </>
          ) : (
            <>
              <Sun className="w-6 h-6 text-light-text-primary dark:text-dark-text-primary" />
              <span className="text-light-text-primary dark:text-dark-text-primary">Light Mode</span>
            </>
          )}
        </button>

        {/* Settings Menu */}
        <div className="relative">
          <button
            onClick={() => setShowMenu(!showMenu)}
            className="nav-link w-full"
          >
            <Menu className="w-6 h-6 text-light-text-primary dark:text-dark-text-primary" />
            <span className="text-light-text-primary dark:text-dark-text-primary">More</span>
          </button>

          {/* Dropdown Menu */}
          {showMenu && (
            <>
              <div
                className="fixed inset-0"
                onClick={() => setShowMenu(false)}
              />
              <div className="absolute bottom-full left-0 mb-2 w-full bg-light-surface rounded-lg shadow-lg border border-light-border overflow-hidden">
                <button
                  onClick={() => {
                    setShowMenu(false);
                    navigate('/settings');
                  }}
                  className="w-full px-4 py-3 text-left flex items-center gap-3 text-sm text-light-text-primary hover:brightness-95 transition-all"
                >
                  <SettingsIcon className="w-5 h-5" />
                  <span>Settings</span>
                </button>
                <div className="divider" />
                <button
                  onClick={() => {
                    setShowMenu(false);
                    handleLogout();
                  }}
                  className="w-full px-4 py-3 text-left flex items-center gap-3 text-sm text-red-500 hover:brightness-95 transition-all"
                >
                  <LogOut className="w-5 h-5" />
                  <span>Log Out</span>
                </button>
              </div>
            </>
          )}
        </div>

        {/* User Info */}
        {user && (
          <div className="px-3 py-2 rounded-lg">
            <p className="text-sm font-medium truncate text-light-text-primary dark:text-dark-text-primary">
              {user.displayName}
            </p>
            <p className="text-xs truncate text-light-text-secondary dark:text-dark-text-secondary">
              @{user.campusDomain}
            </p>
          </div>
        )}
      </div>
    </aside>
  );
}
