// ============================================================================
// Shared TypeScript Types and Interfaces for CollegeBuddy Frontend
// ============================================================================

// User & Profile Types
// ============================================================================

export interface UserDto {
  id: number;
  email: string;
  displayName: string;
  campusDomain: string;
  avatarUrl?: string;
  profileVisibility: 'PUBLIC' | 'PRIVATE';
  role?: 'STUDENT' | 'ADMIN';
}

export interface ProfileDto {
  userId: number;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
  profileVisibility: 'PUBLIC' | 'PRIVATE';
}

// Authentication Types
// ============================================================================

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  displayName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  displayName: string;
  campusDomain: string;
}

export interface VerifyRequest {
  token: string;
}

// Connection Types
// ============================================================================

export type ConnectionStatus = 'CONNECTED' | 'PENDING' | 'NONE' | 'YOU';

export interface ConnectionRequestDto {
  requesterId: number;
  requesterName: string;
  requesterAvatar?: string;
  requestedId: number;
  requestedName: string;
  requestedAvatar?: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
}

export interface ConnectionDto {
  userId: number;
  displayName: string;
  avatarUrl?: string;
  campusDomain: string;
  unreadCount?: number;
}

export interface RespondToConnectionDto {
  otherUserId: number;
  accept: boolean;
}

export interface ConnectionStatusDto {
  otherUserId: number;
  status: ConnectionStatus;
}

// Search Types
// ============================================================================

export interface SearchResultDto {
  userId: number;
  displayName: string;
  campusDomain: string;
  avatarUrl?: string;
  connectionStatus: ConnectionStatus;
}

export interface SearchRequest {
  query: string;
}

// Messaging Types
// ============================================================================

export interface MessageDto {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  timestamp: string;
  isRead: boolean;
}

export interface ConversationDto {
  otherUserId: number;
  otherUserName: string;
  otherUserAvatar?: string;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
}

export interface SendMessageRequest {
  receiverId: number;
  content: string;
}

// Notification Types
// ============================================================================

export interface NotificationDto {
  id: string;
  type: 'CONNECTION_REQUEST' | 'CONNECTION_ACCEPTED' | 'NEW_MESSAGE' | 'NEW_GROUP_MESSAGE' | 'PROFILE_VIEW';
  userId?: number; // Optional for group messages
  userName?: string; // Optional for group messages
  userAvatar?: string;
  groupId?: number; // For group message notifications
  groupName?: string; // For group message notifications
  message: string;
  timestamp: string;
  isRead: boolean;
}

// Dashboard/Stats Types
// ============================================================================

export interface DashboardStats {
  pendingRequestsCount: number;
  unreadMessagesCount: number;
  connectionsCount: number;
  profileViews?: number;
}

// UI State Types
// ============================================================================

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
  duration?: number;
}

export interface Modal {
  isOpen: boolean;
  title?: string;
  content?: React.ReactNode;
  onConfirm?: () => void;
  onCancel?: () => void;
}

// Theme Types
// ============================================================================

export type Theme = 'light' | 'dark';

export interface ThemeContextType {
  theme: Theme;
  toggleTheme: () => void;
}

// Auth Context Types
// ============================================================================

export interface AuthContextType {
  isAuthenticated: boolean;
  user: UserDto | null;
  token: string | null;
  login: (token: string, user: UserDto) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

// API Response Types
// ============================================================================

export interface ApiError {
  message: string;
  status?: number;
  errors?: Record<string, string[]>;
}

export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
}

// Form Types
// ============================================================================

export interface ProfileFormData {
  displayName: string;
  bio: string;
  profileVisibility: 'PUBLIC' | 'PRIVATE';
}

export interface AvatarUploadData {
  file: File;
}

// Component Props Types
// ============================================================================

export interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
  loading?: boolean;
  disabled?: boolean;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  children: React.ReactNode;
  className?: string;
}

export interface AvatarProps {
  src?: string;
  alt: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  fallback?: string;
}

export interface BadgeProps {
  count: number;
  max?: number;
  showZero?: boolean;
  className?: string;
}

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  showCloseButton?: boolean;
  size?: 'sm' | 'md' | 'lg' | 'full';
}

export interface InputProps {
  type?: 'text' | 'email' | 'password' | 'number' | 'tel';
  label?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  disabled?: boolean;
  required?: boolean;
  className?: string;
  showPasswordToggle?: boolean;
}

export interface TextAreaProps {
  label?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  disabled?: boolean;
  required?: boolean;
  rows?: number;
  maxLength?: number;
  showCount?: boolean;
  className?: string;
}
