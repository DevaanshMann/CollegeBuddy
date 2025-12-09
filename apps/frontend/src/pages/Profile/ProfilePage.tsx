import { useEffect, useState } from 'react';
import type { FormEvent, ChangeEvent } from 'react';
import { useLocation } from 'react-router-dom';
import { Settings, Upload, Camera, Globe, Lock } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { apiClient } from '../../api/client';
import { API_BASE_URL } from '../../config';
import { Avatar, Button, Input, TextArea } from '../../components/ui';
import toast from 'react-hot-toast';

type ProfileDto = {
  displayName: string;
  bio: string;
  avatarUrl: string;
  visibility: string;
};

const emptyProfile: ProfileDto = {
  displayName: '',
  bio: '',
  avatarUrl: '',
  visibility: 'PUBLIC',
};

export function ProfilePage() {
  const { user } = useAuth();
  const location = useLocation();
  const [profile, setProfile] = useState<ProfileDto>(emptyProfile);
  const [editedProfile, setEditedProfile] = useState<ProfileDto>(emptyProfile);
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [connectionsCount, setConnectionsCount] = useState(0);
  const [pendingRequestsCount, setPendingRequestsCount] = useState(0);

  useEffect(() => {
    console.log('ProfilePage mounted/navigated, loading profile...');
    void loadProfile();
    void loadConnectionStats();
  }, [location.pathname, user?.id]);

  const getAvatarUrl = (url: string | null | undefined): string | undefined => {
    if (!url) return undefined;
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    if (url.startsWith('/')) {
      return `${API_BASE_URL}${url}`;
    }
    return url;
  };

  async function loadProfile() {
    setLoading(true);

    if (!user?.id) {
      setLoading(false);
      return;
    }

    try {
      const data = await apiClient.get<ProfileDto>(`/profile/${user.id}`);
      console.log('Profile loaded:', data);
      setProfile(data);
      setEditedProfile(data);
    } catch (err: any) {
      console.error('Failed to load profile:', err);
      console.error('Error details:', err.message);
      setProfile(emptyProfile);
      setEditedProfile(emptyProfile);
      setIsEditing(true);
    } finally {
      setLoading(false);
    }
  }

  async function loadConnectionStats() {
    try {
      const connectionsData = await apiClient.get<{
        connections: any[];
        incomingRequests: any[];
        outgoingRequests: any[];
      }>('/connections');

      setConnectionsCount(connectionsData.connections.length);
      setPendingRequestsCount(connectionsData.incomingRequests.length);
    } catch (error) {
      console.error('Failed to load connection stats:', error);
    }
  }

  async function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error('Please select an image file');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toast.error('File size must be less than 5MB');
      return;
    }

    setSelectedFile(file);

    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result as string);
    };
    reader.readAsDataURL(file);
  }

  async function handleUploadAvatar() {
    if (!selectedFile) return;

    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      const token = localStorage.getItem('collegebuddy_jwt');
      const response = await fetch(`${API_BASE_URL}/profile/upload-avatar`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`Upload failed: ${response.status}`);
      }

      const data = await response.json();
      const avatarUrl = data.avatarUrl;

      setEditedProfile({ ...editedProfile, avatarUrl });
      setProfile({ ...profile, avatarUrl });

      setSelectedFile(null);
      setPreviewUrl(null);
      toast.success('Avatar uploaded! Click "Save Profile" to confirm.');
    } catch (err: any) {
      console.error('Upload error:', err);
      toast.error(err.message ?? 'Failed to upload avatar');
    } finally {
      setUploading(false);
    }
  }

  async function handleSave(e: FormEvent) {
    e.preventDefault();
    setSaving(true);

    try {
      console.log('Saving profile with data:', editedProfile);
      await apiClient.put('/profile', editedProfile);
      setProfile(editedProfile);
      setIsEditing(false);
      toast.success('Profile saved successfully!');
      await loadProfile(); // Reload to get latest data
    } catch (err: any) {
      console.error('Save profile error:', err);
      toast.error(err.message ?? 'Failed to save profile');
    } finally {
      setSaving(false);
    }
  }

  function handleEdit() {
    setEditedProfile(profile);
    setIsEditing(true);
    setSelectedFile(null);
    setPreviewUrl(null);
  }

  function handleCancel() {
    setEditedProfile(profile);
    setIsEditing(false);
    setSelectedFile(null);
    setPreviewUrl(null);
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  const avatarUrl = getAvatarUrl(profile.avatarUrl);

  // View Mode - Instagram Style
  if (!isEditing) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Profile Header */}
        <div className="flex flex-col md:flex-row gap-8 md:gap-12 items-start md:items-center mb-8">
          {/* Avatar */}
          <div className="flex-shrink-0">
            <Avatar
              src={avatarUrl}
              alt={profile.displayName}
              size="xl"
              fallback={profile.displayName}
              className="w-32 h-32 md:w-40 md:h-40 border-4 border-gray-200 dark:border-gray-700"
            />
          </div>

          {/* Info */}
          <div className="flex-1 w-full">
            {/* Username & Edit Button */}
            <div className="flex items-center gap-4 mb-6">
              <h1 className="text-2xl font-light text-light-text-primary dark:text-dark-text-primary">
                {profile.displayName || 'No name set'}
              </h1>
              <Button
                variant="secondary"
                size="sm"
                onClick={handleEdit}
                className="gap-2"
              >
                <Settings className="w-4 h-4" />
                Edit Profile
              </Button>
            </div>

            {/* Stats */}
            <div className="flex gap-8 mb-6">
              <div className="text-center md:text-left">
                <span className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                  {connectionsCount}
                </span>
                <span className="text-light-text-secondary dark:text-dark-text-secondary ml-1">
                  connections
                </span>
              </div>
              <div className="text-center md:text-left">
                <span className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                  {pendingRequestsCount}
                </span>
                <span className="text-light-text-secondary dark:text-dark-text-secondary ml-1">
                  pending
                </span>
              </div>
            </div>

            {/* Bio */}
            <div className="space-y-1">
              <p className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                {user?.displayName}
              </p>
              {profile.bio && (
                <p className="text-sm text-light-text-primary dark:text-dark-text-primary whitespace-pre-wrap">
                  {profile.bio}
                </p>
              )}
              <p className="text-xs text-light-text-secondary dark:text-dark-text-secondary flex items-center gap-1">
                {profile.visibility === 'PUBLIC' ? (
                  <>
                    <Globe className="w-3 h-3" />
                    Public Profile
                  </>
                ) : (
                  <>
                    <Lock className="w-3 h-3" />
                    Private Profile
                  </>
                )}
              </p>
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="divider mb-8" />

        {/* Additional Info Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="card p-6">
            <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
              Campus
            </h3>
            <p className="text-light-text-secondary dark:text-dark-text-secondary">
              @{user?.campusDomain}
            </p>
          </div>
          <div className="card p-6">
            <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
              Account Status
            </h3>
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 bg-green-500 rounded-full"></span>
              <p className="text-light-text-secondary dark:text-dark-text-secondary">
                Active
              </p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Edit Mode
  const editAvatarUrl = previewUrl || getAvatarUrl(editedProfile.avatarUrl);

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2">
          Edit Profile
        </h2>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Update your profile information and settings
        </p>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        {/* Avatar Section */}
        <div className="card p-6">
          <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-4">
            Profile Picture
          </h3>
          <div className="flex flex-col sm:flex-row items-center gap-6">
            <Avatar
              src={editAvatarUrl}
              alt={editedProfile.displayName}
              size="xl"
              fallback={editedProfile.displayName}
              className="w-24 h-24"
            />
            <div className="flex-1 space-y-3">
              <div className="relative">
                <input
                  type="file"
                  id="avatar-upload"
                  accept="image/*"
                  onChange={handleFileChange}
                  className="hidden"
                />
                <label
                  htmlFor="avatar-upload"
                  className="btn-secondary cursor-pointer inline-flex items-center gap-2"
                >
                  <Camera className="w-4 h-4" />
                  Choose Photo
                </label>
              </div>
              {selectedFile && (
                <Button
                  type="button"
                  variant="primary"
                  size="sm"
                  onClick={handleUploadAvatar}
                  loading={uploading}
                  className="gap-2"
                >
                  <Upload className="w-4 h-4" />
                  Upload Avatar
                </Button>
              )}
              <p className="text-xs text-light-text-secondary dark:text-dark-text-secondary">
                JPG, PNG or GIF (max 5MB)
              </p>
            </div>
          </div>
        </div>

        {/* Profile Info */}
        <div className="card p-6 space-y-4">
          <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
            Profile Information
          </h3>

          <Input
            label="Display Name"
            value={editedProfile.displayName}
            onChange={(value) =>
              setEditedProfile({ ...editedProfile, displayName: value })
            }
            required
            placeholder="Enter your display name"
          />

          <TextArea
            label="Bio"
            value={editedProfile.bio}
            onChange={(value) => {
              console.log('Bio changed to:', value);
              setEditedProfile({ ...editedProfile, bio: value });
            }}
            placeholder="Tell others about yourself..."
            rows={4}
            maxLength={500}
            showCount
          />
        </div>

        {/* Privacy Settings */}
        <div className="card p-6">
          <h3 className="font-semibold text-light-text-primary dark:text-dark-text-primary mb-4">
            Privacy Settings
          </h3>
          <div className="space-y-3">
            <label className="flex items-center gap-3 p-4 bg-light-surface dark:bg-dark-bg rounded-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <input
                type="radio"
                name="visibility"
                value="PUBLIC"
                checked={editedProfile.visibility === 'PUBLIC'}
                onChange={(e) =>
                  setEditedProfile({
                    ...editedProfile,
                    visibility: e.target.value,
                  })
                }
                className="w-4 h-4 text-blue-500"
              />
              <div className="flex-1">
                <div className="flex items-center gap-2 font-medium text-light-text-primary dark:text-dark-text-primary">
                  <Globe className="w-4 h-4" />
                  Public Profile
                </div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mt-1">
                  Your profile will be visible to all students at your campus
                </p>
              </div>
            </label>

            <label className="flex items-center gap-3 p-4 bg-light-surface dark:bg-dark-bg rounded-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              <input
                type="radio"
                name="visibility"
                value="PRIVATE"
                checked={editedProfile.visibility === 'PRIVATE'}
                onChange={(e) =>
                  setEditedProfile({
                    ...editedProfile,
                    visibility: e.target.value,
                  })
                }
                className="w-4 h-4 text-blue-500"
              />
              <div className="flex-1">
                <div className="flex items-center gap-2 font-medium text-light-text-primary dark:text-dark-text-primary">
                  <Lock className="w-4 h-4" />
                  Private Profile
                </div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mt-1">
                  Only you can see your profile
                </p>
              </div>
            </label>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-3">
          <Button type="submit" variant="primary" fullWidth loading={saving}>
            Save Profile
          </Button>
          <Button
            type="button"
            variant="secondary"
            fullWidth
            onClick={handleCancel}
            disabled={saving}
          >
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
}
