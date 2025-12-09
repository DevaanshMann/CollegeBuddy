import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../../api/client';
import { Button, Input } from '../../components/ui';
import { useAuth } from '../../contexts/AuthContext';
import { JWT_STORAGE_KEY } from '../../config';
import toast from 'react-hot-toast';

type LoginResponse = {
  status: string;
  jwt: string;
};

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await apiClient.post<LoginResponse>('/auth/login', {
        email,
        password,
      });

      if (res.jwt) {
        // Store JWT in localStorage BEFORE calling /auth/me
        localStorage.setItem(JWT_STORAGE_KEY, res.jwt);

        // Fetch user data from /auth/me endpoint
        const userRes = await apiClient.get<any>('/auth/me');

        // Use AuthContext login function with server-validated user data
        login(res.jwt, userRes);

        // Navigate to home
        navigate('/home');
        toast.success('Welcome back!');
      } else {
        toast.error('Login failed - no token received');
      }
    } catch (err: any) {
      console.error('Login error:', err);
      toast.error(err.message ?? 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col justify-center items-center px-4 bg-light-bg dark:bg-dark-bg">
      {/* Logo */}
      <h1 className="text-5xl font-bold mb-8 text-blue-500" style={{ fontFamily: "'Pacifico', cursive" }}>
        CollegeBuddy
      </h1>

      {/* Login Card */}
      <div className="card w-full max-w-md p-8">
        <h2 className="text-2xl font-bold text-center mb-6 text-light-text-primary dark:text-dark-text-primary">
          Log In
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            type="email"
            label="Email (.edu)"
            value={email}
            onChange={setEmail}
            required
            placeholder="your.email@university.edu"
          />

          <Input
            type="password"
            label="Password"
            value={password}
            onChange={setPassword}
            required
            placeholder="Enter your password"
            showPasswordToggle
          />

          <div className="text-right">
            <Link to="/forgot-password" className="text-sm text-blue-500 hover:text-blue-600">
              Forgot password?
            </Link>
          </div>

          <Button type="submit" variant="primary" fullWidth loading={loading}>
            Log In
          </Button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Don't have an account?{' '}
            <Link to="/signup" className="text-blue-500 hover:text-blue-600 font-semibold">
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
