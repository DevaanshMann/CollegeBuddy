import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { AuthContextType, UserDto } from '../types';
import { JWT_STORAGE_KEY } from '../config';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem(JWT_STORAGE_KEY);
  });

  const [user, setUser] = useState<UserDto | null>(null);

  useEffect(() => {
    // Decode token and set user if token exists
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          id: payload.sub,
          email: payload.email || '',
          displayName: payload.displayName || '',
          campusDomain: payload.campusDomain || '',
          profileVisibility: 'PUBLIC'
        });
      } catch (error) {
        // Invalid token, clear it
        logout();
      }
    }
  }, [token]);

  const login = (newToken: string, userData: UserDto) => {
    localStorage.setItem(JWT_STORAGE_KEY, newToken);
    setToken(newToken);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem(JWT_STORAGE_KEY);
    setToken(null);
    setUser(null);
  };

  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
