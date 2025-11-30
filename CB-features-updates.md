# CollegeBuddy - Planned Feature Updates

## Feature 1: Blocking a User

### Core Requirements (Must Implement)

#### Data Model
- Create `UserBlock` entity with fields: blockerId, blockedUserId, createdAt
- Unique constraint on (blockerId, blockedUserId) pair
- Campus domain validation still applies

#### Blocking Mechanics
- User can block another user regardless of connection status
- Block action is unidirectional (A blocks B doesn't mean B blocks A)
- Blocked user cannot see blocker's profile details
- Blocked user cannot send connection requests to blocker
- Blocked user cannot send messages to blocker

#### Impact on Existing Connections
- If users are connected when block occurs, automatically disconnect them
- Delete conversation when block occurs (or keep but hide)
- Remove any pending connection requests (both directions)

#### Search & Discovery
- Blocked users should not appear in blocker's search results
- Blocker should not appear in blocked user's search results

#### API Endpoints
- POST /api/users/{userId}/block - Block a user
- DELETE /api/users/{userId}/block - Unblock a user
- GET /api/users/blocked - List all blocked users

### Optional Enhancements (Could Implement)

#### User Experience
- Confirmation dialog before blocking
- Reason for blocking (dropdown: harassment, spam, other)
- Block limit per user to prevent abuse (e.g., max 100 blocks)

#### Privacy & Communication
- When blocked user tries to send connection request, show generic "Unable to send request" (don't reveal they're blocked)
- When blocked user views blocker's profile (if accessible), show minimal info or "User unavailable"

#### Block Management
- Unblock functionality with optional cooldown period (e.g., can't re-block for 24 hours)
- View block history with timestamps

#### Edge Cases to Handle
- What if both users block each other?
- Prevent blocking and unblocking same user repeatedly in short timespan
- If user A blocks user B, then B blocks A, what happens on unblock?

---

## Feature 2: Deleting an Account

### Core Requirements (Must Implement)

#### Deletion Strategy
- Soft delete vs hard delete decision:
  - **Soft delete**: Add `deletedAt` timestamp to User entity, keep data in DB
  - **Hard delete**: Permanently remove all user data

#### Data Cleanup (if hard delete)
- Delete user record from `users` table
- Delete associated `Profile` record
- Delete `VerificationToken` records
- Delete uploaded avatar files from filesystem
- Handle connections where user is either userA or userB
- Handle connection requests sent by or to the user
- Handle messages sent by the user

#### Message Handling Decision
- Option 1: Delete all messages (affects conversation history for others)
- Option 2: Keep messages but anonymize sender (change senderId to null or special "deleted user" ID)
- Option 3: Replace message content with "[deleted]" placeholder

#### Account Deletion Process
- Require password confirmation before deletion
- Optional: Send confirmation email with deletion link
- Add AccountStatus.PENDING_DELETION state

#### What Other Users See
- Deleted user's profile shows "Account deleted" or similar
- In conversations, either remove messages or show as "[Deleted User]"
- Remove from all connection lists

### Optional Enhancements (Could Implement)

#### Grace Period
- 30-day grace period before permanent deletion
- User can cancel deletion during grace period
- Account status: PENDING_DELETION during grace period
- Schedule background job to purge after grace period

#### Data Export (GDPR Compliance)
- Allow user to download their data before deletion
- Export includes: profile data, messages, connections, timestamps
- Format: JSON or CSV

#### Deletion Notification
- Email confirmation when deletion is initiated
- Email notification when deletion is completed (if grace period)
- Notify connections that user deleted their account (optional)

#### Referential Integrity
- Use foreign key constraints with CASCADE or SET NULL
- Handle orphaned records (conversations with deleted participants)
- Clean up conversations table where both users are deleted

#### Security Considerations
- Log deletion events for audit trail
- Prevent account recovery after hard delete
- Allow account recovery during grace period only
- Verify user identity (password + optional 2FA) before deletion

#### Edge Cases to Handle
- User deletes account while having pending connection requests
- User deletes account while in active conversations
- What if user tries to recreate account with same email?
- Handle active sessions/JWT tokens after deletion
- Scheduled messages or future-dated actions

#### Admin Considerations
- Admin ability to restore accidentally deleted accounts (if soft delete)
- Compliance logging for legal requirements
- Automated cleanup of associated files and storage

---

## Implementation Priorities

### For Blocking
1. Basic block/unblock functionality
2. Impact on connections and requests
3. Search filtering
4. Profile visibility restrictions
5. Message prevention

### For Account Deletion
1. Choose soft vs hard delete strategy
2. Basic deletion flow with password confirmation
3. Data cleanup logic
4. Handle message anonymization
5. Add grace period (recommended)
6. Implement data export (GDPR)

---

## Database Schema Considerations

### UserBlock Table
```sql
CREATE TABLE user_blocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    FOREIGN KEY (blocker_id) REFERENCES users(id),
    FOREIGN KEY (blocked_id) REFERENCES users(id),
    UNIQUE KEY unique_block (blocker_id, blocked_id)
);
```

### User Table Update
```sql
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL;
-- status enum already exists, add PENDING_DELETION to AccountStatus enum
```

---

# Frontend Updates

## Current State Assessment

**Tech Stack:** React 19 + TypeScript + Vite
**Lines of Code:** ~2,381 lines
**Components:** Only 1 reusable component (NavBar)
**Styling:** 100% inline styles (no CSS framework)
**State Management:** Local component state only (no global state)
**Testing:** No tests implemented
**Production Readiness:** 4/10

## Enhancement Areas

### 1. Styling & Design System (CRITICAL)

#### Current Issues
- 100% inline styles using React's `style` prop
- No CSS framework, no design system
- Inconsistent colors (multiple shades of blue, gray, etc.)
- No CSS variables or theme tokens
- Magic numbers everywhere
- Poor responsive design (hardcoded heights like 500px)
- Emoji overuse (accessibility concerns)

#### Recommended Solutions
- **Implement Tailwind CSS** or styled-components for maintainable styling
- Create design system with:
  - Color palette as CSS variables
  - Typography scale
  - Spacing system (4px, 8px, 16px, 24px, 32px, etc.)
  - Component variants (primary, secondary, danger buttons)
  - Responsive breakpoints (sm, md, lg, xl)
- Replace emojis with proper icon library (React Icons, Lucide Icons)
- Implement dark mode support with theme switching
- Add smooth transitions and animations (framer-motion)

#### Implementation Priority: P0

---

### 2. Component Architecture (CRITICAL)

#### Current Issues
- Only 1 reusable component (NavBar)
- Massive page components (ProfilePage: 569 lines, ConnectionsPage: 404 lines)
- Button styles duplicated across all pages
- Modal code duplicated (disconnect confirmation)
- User cards not componentized
- Form inputs not abstracted
- No component composition

#### Components to Extract

**UI Components:**
- `Button` (primary, secondary, danger, ghost variants)
- `Input` (text, email, password, textarea variants)
- `Modal` / `Dialog` (confirmation, info, forms)
- `Card` (with header, body, footer sections)
- `Avatar` (with size variants, fallback initials)
- `Badge` (for unread counts, status indicators)
- `LoadingSpinner` / `Skeleton`
- `EmptyState` (no results, no connections, etc.)
- `ErrorMessage` / `SuccessMessage` / `Toast`

**Feature Components:**
- `UserCard` (for search results, connections list)
- `ConnectionRequest` (incoming/outgoing request item)
- `MessageBubble` (chat message display)
- `ConversationList` (list of chats with unread badges)
- `SearchBar` (with filters, debouncing)
- `ProfileHeader` (avatar + name + bio section)
- `ProfileForm` (edit profile fields)

**Layout Components:**
- `PageContainer` (consistent max-width, padding)
- `Section` (collapsible sections like in ConnectionsPage)
- `Grid` / `Stack` (layout primitives)

#### Shared Types
- Create `/src/types/index.ts` for shared TypeScript interfaces
- Move all DTOs to shared file (UserDto, ConnectionRequestDto, etc.)
- Add API response types, form types, state types

#### Implementation Priority: P0

---

### 3. State Management (HIGH PRIORITY)

#### Current Issues
- No global state management
- Auth state duplicated across components
- Connection status fetched separately in multiple pages
- Unread counts only visible on Connections page
- No real-time updates (requires manual refresh)
- No API response caching

#### Recommended Solutions

**Option A: Context API (Simpler)**
```
- AuthContext (user, token, login, logout, isAuthenticated)
- NotificationContext (unread counts, connection requests)
- ThemeContext (dark/light mode)
```

**Option B: Redux Toolkit (More Scalable)**
```
- authSlice (user state, JWT handling)
- connectionsSlice (friends, requests, unread counts)
- messagesSlice (conversations, message caching)
- uiSlice (theme, modals, toasts)
```

**Option C: Zustand (Best of Both)**
```
- Lighter than Redux, simpler than Context
- Built-in devtools, middleware support
- No provider wrapping needed
```

#### Features to Implement
- Centralized auth state with token refresh logic
- Real-time notification counts
- Optimistic UI updates (instant feedback before API confirms)
- API request caching (React Query or SWR)
- Persistent state (sync with localStorage)

#### Implementation Priority: P1

---

### 4. User Experience Improvements (HIGH PRIORITY)

#### Current Issues
- Minimal form validation (only HTML5 required attribute)
- No client-side validation feedback
- No loading states for navigation
- No optimistic updates
- Confusing UX (avatar upload separate from profile save)
- No image preview before upload
- Full page reloads on logout/login

#### Recommended Solutions

**Form Validation:**
- Implement React Hook Form + Zod schema validation
- Real-time validation feedback
- Password strength requirements
- Email format validation
- Character count for bio (500 limit)
- Image size/type validation before upload

**Loading States:**
- Skeleton screens for data loading
- Button loading spinners during actions
- Page transition loading indicators
- Lazy loading images with blur-up effect

**Optimistic Updates:**
- Instant UI feedback when sending connection request
- Optimistic message sending in chat
- Instant like/unlike reactions

**Better Feedback:**
- Toast notifications for success/error (react-hot-toast)
- Inline error messages
- Success confirmations
- Undo actions where appropriate

**Image Handling:**
- Image cropping tool before upload (react-easy-crop)
- Preview uploaded avatar before saving
- Drag & drop upload support
- Avatar deletion option

**Navigation:**
- Use `navigate()` instead of `window.location.href`
- Preserve scroll position on back navigation
- Breadcrumbs for deep navigation

#### Implementation Priority: P1

---

### 5. Accessibility (HIGH PRIORITY)

#### Current Issues
- No ARIA labels on interactive elements
- Limited semantic HTML
- No keyboard navigation support
- Color contrast not verified (especially with emojis)
- No screen reader support
- Focus management issues

#### Recommended Solutions

**ARIA Implementation:**
- Add `aria-label` to all buttons/links
- Use `role` attributes appropriately
- Add `aria-describedby` for form errors
- Implement `aria-live` regions for notifications
- Add `aria-expanded` for collapsible sections

**Keyboard Navigation:**
- Focus visible indicators
- Tab order management
- Escape to close modals
- Enter to submit forms
- Arrow keys for lists

**Semantic HTML:**
- Use `<nav>`, `<main>`, `<section>`, `<article>`
- Proper heading hierarchy (h1, h2, h3)
- `<button>` instead of `<div onClick>`
- `<form>` for all form submissions

**Color Contrast:**
- Verify WCAG AA compliance (4.5:1 ratio)
- Don't rely on color alone for information
- Add text labels alongside colored indicators

**Screen Reader Support:**
- Alt text for all images
- Descriptive link text (not "click here")
- Form labels properly associated
- Error announcements

#### Implementation Priority: P1

---

### 6. Performance Optimization (MEDIUM PRIORITY)

#### Current Issues
- No code splitting or lazy loading
- All pages bundled together (large initial bundle)
- Unnecessary re-renders (NavBar on every location change)
- No React.memo or useMemo usage
- Images not optimized
- No request deduplication
- No caching strategy
- Potential memory leaks (async operations not canceled on unmount)

#### Recommended Solutions

**Code Splitting:**
```tsx
const ProfilePage = lazy(() => import('./pages/Profile/ProfilePage'))
const SearchPage = lazy(() => import('./pages/Search/SearchPage'))
// ... etc
```

**Memoization:**
- Use `React.memo` for expensive components
- Use `useMemo` for expensive calculations
- Use `useCallback` for event handlers passed to children

**Image Optimization:**
- Compress avatars server-side
- Use WebP format with fallbacks
- Implement lazy loading with Intersection Observer
- Add blur-up placeholders

**Request Optimization:**
- Implement React Query or SWR for automatic caching
- Request deduplication
- Background refetching
- Stale-while-revalidate pattern

**Cleanup:**
- AbortController for fetch requests
- Cleanup in useEffect return functions
- Cancel pending operations on unmount

**Bundle Analysis:**
- Run `npm run build -- --analyze`
- Identify large dependencies
- Consider alternatives or dynamic imports

#### Implementation Priority: P2

---

### 7. Feature Enhancements (MEDIUM PRIORITY)

#### Missing Core Features

**Real-time Chat (WebSocket):**
- Bidirectional communication for instant messaging
- Typing indicators
- Online/offline status
- Message delivery/read receipts
- Push notifications

**Password Management:**
- Forgot password flow
- Password reset via email
- Change password in settings
- Password strength requirements

**Advanced Search:**
- Filter by major, graduation year, interests
- Search pagination or infinite scroll
- Save search filters
- Recent searches

**Messaging Improvements:**
- Pagination for message history
- Message editing (within time limit)
- Message deletion
- File/image sharing
- Emoji picker
- Reply to specific messages
- Search within conversation

**Connection Management:**
- Cancel outgoing connection requests
- Block/unblock users
- Mutual connections display
- Connection suggestions
- Connection message (when sending request)

**Notifications:**
- In-app notification center
- Desktop notifications (Web Push API)
- Email notifications (configurable)
- Notification preferences

**Settings Page:**
- Privacy settings (public/private profile)
- Email preferences
- Notification preferences
- Account management (change email, delete account)
- Theme preferences (dark/light mode)

**Profile Enhancements:**
- Cover photo upload
- Profile completion percentage
- Social links
- Interests/hobbies tags
- Major and graduation year fields
- Bio formatting (bold, italics, links)

#### Implementation Priority: P2

---

### 8. Code Quality & Developer Experience (MEDIUM PRIORITY)

#### Current Issues
- Zero tests (no unit, integration, or E2E tests)
- No error boundaries
- Console.log statements left in code
- Hardcoded URLs and magic strings
- Type definitions duplicated across files
- No code linting enforcement
- No pre-commit hooks

#### Recommended Solutions

**Testing Infrastructure:**
```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
npm install -D @testing-library/user-event msw
```
- Unit tests for components (Button, Input, Modal)
- Integration tests for pages (LoginPage, ProfilePage)
- E2E tests with Playwright or Cypress
- API mocking with MSW (Mock Service Worker)
- Test coverage reporting (aim for 80%+)

**Error Boundaries:**
```tsx
<ErrorBoundary fallback={<ErrorFallback />}>
  <App />
</ErrorBoundary>
```
- Catch React errors gracefully
- Log errors to monitoring service (Sentry)
- Show user-friendly error UI
- Retry mechanisms

**Code Quality Tools:**
- Remove all console.log statements
- Enforce ESLint rules (no-console in production)
- Add Prettier for code formatting
- Add husky + lint-staged for pre-commit hooks
- Add TypeScript strict mode checks

**Constants & Types:**
- Create `/src/constants/routes.ts` for route paths
- Create `/src/constants/api.ts` for API endpoints
- Move shared types to `/src/types/index.ts`
- Create enums for status values

**Documentation:**
- Add JSDoc comments for complex functions
- Component prop documentation
- README for frontend setup
- Storybook for component showcase (optional)

#### Implementation Priority: P2

---

### 9. Security Improvements (HIGH PRIORITY)

#### Current Issues
- JWT decoded client-side without validation
- No token expiry handling
- No refresh token mechanism
- Sensitive data in localStorage (XSS vulnerability)
- No CSRF protection visible
- No rate limiting on client side
- No input sanitization

#### Recommended Solutions

**Token Security:**
- Implement refresh token flow
- Store tokens in httpOnly cookies (backend change needed)
- Automatic token refresh before expiry
- Clear tokens on logout
- Handle 401 responses globally (redirect to login)

**XSS Prevention:**
- Sanitize all user input before display
- Use DOMPurify for HTML content
- Validate file uploads (type, size, content)
- CSP headers (Content Security Policy)

**CSRF Protection:**
- CSRF tokens for state-changing requests
- SameSite cookie attribute
- Origin/Referer checking

**Input Validation:**
- Client-side validation with Zod
- Sanitize before sending to API
- Max length enforcement
- File type whitelisting

**Rate Limiting UI:**
- Debounce search input (300ms)
- Throttle button clicks
- Prevent double form submissions
- Loading states during requests

#### Implementation Priority: P1

---

### 10. Responsive Design (HIGH PRIORITY)

#### Current Issues
- Inconsistent max-width constraints
- No mobile-first approach
- No breakpoints defined
- Chat page height hardcoded to 500px
- Desktop-only layout assumptions
- No touch-friendly tap targets

#### Recommended Solutions

**Breakpoint System:**
```css
mobile: 640px
tablet: 768px
laptop: 1024px
desktop: 1280px
```

**Mobile Optimizations:**
- Hamburger menu for navigation on mobile
- Bottom tab bar for main navigation
- Stack layouts instead of side-by-side
- Touch-friendly button sizes (min 44px)
- Swipe gestures for actions
- Pull-to-refresh

**Layout Patterns:**
- Mobile-first CSS (min-width media queries)
- Flexbox/Grid for responsive layouts
- Container queries for component-level responsiveness
- Responsive typography (clamp, fluid sizing)

**Testing:**
- Test on actual devices
- Chrome DevTools device emulation
- Responsive design mode
- Test landscape and portrait orientations

#### Implementation Priority: P1

---

## Implementation Roadmap

### Phase 1: Foundation (Weeks 1-2)
**Goal:** Establish solid architecture and tooling

- [ ] Set up Tailwind CSS with design system tokens
- [ ] Extract core UI components (Button, Input, Modal, Card)
- [ ] Implement Context API for auth state
- [ ] Add error boundaries
- [ ] Set up testing infrastructure (Vitest + React Testing Library)
- [ ] Add form validation with React Hook Form + Zod
- [ ] Remove console.logs and enforce with ESLint

**Deliverables:**
- Reusable component library
- Global state management
- Production-ready error handling
- Testing framework in place

---

### Phase 2: User Experience (Weeks 3-4)
**Goal:** Polish UX and improve accessibility

- [ ] Implement responsive design for all pages
- [ ] Add loading skeletons and better loading states
- [ ] Implement toast notification system
- [ ] Add optimistic updates for common actions
- [ ] Improve form validation feedback
- [ ] Add ARIA labels and keyboard navigation
- [ ] Implement image cropping for avatar upload
- [ ] Add smooth page transitions

**Deliverables:**
- Mobile-responsive app
- Accessible UI (WCAG AA compliant)
- Polished user feedback system
- Better loading and error states

---

### Phase 3: Performance (Week 5)
**Goal:** Optimize for speed and efficiency

- [ ] Implement code splitting with React.lazy
- [ ] Add React Query for API caching
- [ ] Memoize expensive components
- [ ] Optimize images and assets
- [ ] Add request deduplication
- [ ] Implement proper cleanup in useEffect hooks
- [ ] Analyze bundle size and optimize

**Deliverables:**
- Faster initial load time
- Reduced re-renders
- Better perceived performance
- Smaller bundle size

---

### Phase 4: Advanced Features (Weeks 6-7)
**Goal:** Add missing features and enhancements

- [ ] Implement WebSocket for real-time chat
- [ ] Add dark mode support
- [ ] Create settings page
- [ ] Implement password reset flow
- [ ] Add notification center
- [ ] Implement advanced search filters
- [ ] Add pagination for search and messages
- [ ] Implement message editing/deletion

**Deliverables:**
- Real-time chat experience
- Dark mode toggle
- Complete user settings
- Enhanced search functionality

---

### Phase 5: Testing & Polish (Week 8)
**Goal:** Ensure quality and production readiness

- [ ] Write unit tests for all components (80% coverage)
- [ ] Write integration tests for all pages
- [ ] Add E2E tests for critical user flows
- [ ] Security audit and fixes
- [ ] Performance audit with Lighthouse
- [ ] Accessibility audit with axe DevTools
- [ ] Cross-browser testing
- [ ] Final bug fixes and polish

**Deliverables:**
- Comprehensive test suite
- Security hardened
- Performance optimized (90+ Lighthouse score)
- Production-ready application

---

## Success Metrics

### Code Quality
- [ ] 80%+ test coverage
- [ ] 0 console.log statements in production
- [ ] 0 ESLint errors
- [ ] TypeScript strict mode enabled
- [ ] All components documented

### Performance
- [ ] Lighthouse Performance Score: 90+
- [ ] First Contentful Paint: < 1.5s
- [ ] Time to Interactive: < 3s
- [ ] Bundle size: < 200KB gzipped

### Accessibility
- [ ] WCAG AA compliant
- [ ] Keyboard navigation fully functional
- [ ] Screen reader compatible
- [ ] Color contrast ratio: 4.5:1+

### User Experience
- [ ] Mobile-responsive (tested on 5+ devices)
- [ ] Loading states on all async actions
- [ ] Error handling on all forms
- [ ] Optimistic updates for common actions
- [ ] Dark mode support

---

## Recommended Tech Stack Additions

### Core Dependencies
```bash
# Styling
npm install tailwindcss postcss autoprefixer
npm install @headlessui/react  # Accessible UI components
npm install lucide-react        # Icon library

# State Management
npm install zustand             # Or @reduxjs/toolkit
npm install @tanstack/react-query  # API state management

# Forms
npm install react-hook-form zod @hookform/resolvers

# UI/UX
npm install framer-motion       # Animations
npm install react-hot-toast     # Notifications
npm install react-easy-crop     # Image cropping

# Real-time
npm install socket.io-client    # WebSocket

# Utils
npm install date-fns            # Date formatting
npm install clsx                # Conditional classnames
npm install dompurify           # XSS prevention
```

### Dev Dependencies
```bash
# Testing
npm install -D vitest @testing-library/react @testing-library/jest-dom
npm install -D @testing-library/user-event msw

# Code Quality
npm install -D prettier eslint-config-prettier
npm install -D husky lint-staged

# Performance
npm install -D vite-plugin-compression
npm install -D rollup-plugin-visualizer
```

---

## Conclusion

The CollegeBuddy frontend has a solid foundation but needs significant enhancements to reach production quality. The proposed improvements will transform it from an MVP prototype (4/10 production readiness) to a polished, accessible, performant application (9/10).

**Estimated Total Timeline:** 8 weeks for full implementation
**Recommended Approach:** Phased rollout starting with Phase 1 (Foundation)
