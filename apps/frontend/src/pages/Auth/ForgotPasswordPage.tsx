import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../../api/client';
import { Button, Input } from '../../components/ui';
import toast from 'react-hot-toast';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);

    try {
      await apiClient.post('/auth/forgot-password', { email });
      setEmailSent(true);
      toast.success('Password reset email sent! Check your inbox.');
    } catch (err: any) {
      console.error('Forgot password error:', err);
      toast.error(err.message ?? 'Failed to send reset email');
    } finally {
      setLoading(false);
    }
  }

  if (emailSent) {
    return (
      <div className="min-h-screen flex flex-col justify-center items-center px-4 bg-light-bg dark:bg-dark-bg">
        <h1 className="text-5xl font-bold mb-8 text-blue-500" style={{ fontFamily: "'Pacifico', cursive" }}>
          CollegeBuddy
        </h1>

        <div className="card w-full max-w-md p-8 text-center">
          <div className="w-16 h-16 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>

          <h2 className="text-2xl font-bold mb-4 text-light-text-primary dark:text-dark-text-primary">
            Check Your Email
          </h2>

          <p className="text-light-text-secondary dark:text-dark-text-secondary mb-6">
            We've sent password reset instructions to <strong>{email}</strong>.
            The link will expire in 15 minutes.
          </p>

          <Link to="/login">
            <Button variant="primary" fullWidth>
              Back to Login
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col justify-center items-center px-4 bg-light-bg dark:bg-dark-bg">
      <h1 className="text-5xl font-bold mb-8 text-blue-500" style={{ fontFamily: "'Pacifico', cursive" }}>
        CollegeBuddy
      </h1>

      <div className="card w-full max-w-md p-8">
        <h2 className="text-2xl font-bold text-center mb-2 text-light-text-primary dark:text-dark-text-primary">
          Forgot Password?
        </h2>

        <p className="text-center text-light-text-secondary dark:text-dark-text-secondary mb-6">
          Enter your email address and we'll send you a link to reset your password.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            type="email"
            label="Email (.edu)"
            value={email}
            onChange={setEmail}
            required
            placeholder="your.email@university.edu"
          />

          <Button type="submit" variant="primary" fullWidth loading={loading}>
            Send Reset Link
          </Button>
        </form>

        <div className="mt-6 text-center">
          <Link to="/login" className="text-blue-500 hover:text-blue-600 font-semibold">
            Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}
