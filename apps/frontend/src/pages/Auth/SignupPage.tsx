import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../../api/client';
import { Button, Input } from '../../components/ui';
import toast from 'react-hot-toast';

type SignupResponse = {
  status: string;
  jwt: string | null;
};

export function SignupPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [campusDomain, setCampusDomain] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await apiClient.post<SignupResponse>('/auth/signup', {
        email,
        password,
        campusDomain,
      });
      toast.success(
        `${res.status}. Check your email for verification link!`
      );
    } catch (err: any) {
      console.error('Signup error:', err);
      toast.error(err.message ?? 'Signup failed');
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

      {/* Signup Card */}
      <div className="card w-full max-w-md p-8">
        <h2 className="text-2xl font-bold text-center mb-6 text-light-text-primary dark:text-dark-text-primary">
          Sign Up
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
            placeholder="Create a password"
          />

          <Input
            type="text"
            label="Campus Domain"
            value={campusDomain}
            onChange={setCampusDomain}
            required
            placeholder="e.g., neu.edu"
          />

          <Button type="submit" variant="primary" fullWidth loading={loading}>
            Create Account
          </Button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-500 hover:text-blue-600 font-semibold">
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
