import { useNavigate } from 'react-router-dom';
import { Settings as SettingsIcon, UserX, Trash2, Shield, ChevronRight } from 'lucide-react';

export function SettingsPage() {
  const navigate = useNavigate();

  const settingsSections = [
    {
      title: 'Privacy & Safety',
      items: [
        {
          icon: UserX,
          label: 'Blocked Users',
          description: 'Manage users you have blocked',
          path: '/settings/blocked-users',
          color: 'text-orange-500',
        },
      ],
    },
    {
      title: 'Account',
      items: [
        {
          icon: Trash2,
          label: 'Delete Account',
          description: 'Permanently delete your account and data',
          path: '/settings/delete-account',
          color: 'text-red-500',
        },
      ],
    },
  ];

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2 flex items-center gap-2">
          <SettingsIcon className="w-6 h-6" />
          Settings
        </h1>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Manage your account settings and preferences
        </p>
      </div>

      {/* Settings Sections */}
      <div className="space-y-8">
        {settingsSections.map((section, sectionIndex) => (
          <div key={sectionIndex}>
            <h2 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-4">
              {section.title}
            </h2>
            <div className="space-y-3">
              {section.items.map((item, itemIndex) => {
                const Icon = item.icon;
                return (
                  <button
                    key={itemIndex}
                    onClick={() => navigate(item.path)}
                    className="w-full flex items-center justify-between p-4 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors group"
                  >
                    <div className="flex items-center gap-4">
                      <div className={`flex-shrink-0 ${item.color}`}>
                        <Icon className="w-6 h-6" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                          {item.label}
                        </p>
                        <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                          {item.description}
                        </p>
                      </div>
                    </div>
                    <ChevronRight className="w-5 h-5 text-gray-500 dark:text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300 transition-colors" />
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* Info Card */}
      <div className="mt-8 p-6 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg">
        <div className="flex gap-3">
          <Shield className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
          <div>
            <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-1">
              Your Privacy Matters
            </h3>
            <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
              We take your privacy seriously. You have full control over your data and who can interact with you on CollegeBuddy.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
