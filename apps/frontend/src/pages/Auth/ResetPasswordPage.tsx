import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { apiClient } from '../../api/client';
import { Button, Input } from '../../components/ui';
import toast from 'react-hot-toast';

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token) {
      toast.error('Invalid or missing reset token');
      navigate('/login');
    }
  }, [token, navigate]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();

    if (newPassword.length < 8) {
      toast.error('Password must be at least 8 characters');
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    setLoading(true);

    try {
      await apiClient.post('/auth/reset-password', {
        token,
        newPassword,
      });

      toast.success('Password reset successfully!');
      navigate('/login');
    } catch (err: any) {
      console.error('Reset password error:', err);
      toast.error(err.message ?? 'Failed to reset password');
    } finally {
      setLoading(false);
    }
  }

  if (!token) {
    return null;
  }

  return (
    <div className="min-h-screen flex flex-col justify-center items-center px-4 bg-light-bg dark:bg-dark-bg">
      <h1 className="text-5xl font-bold mb-8 text-blue-500" style={{ fontFamily: "'Pacifico', cursive" }}>
        CollegeBuddy
      </h1>

      <div className="card w-full max-w-md p-8">
        <h2 className="text-2xl font-bold text-center mb-2 text-light-text-primary dark:text-dark-text-primary">
          Reset Password
        </h2>

        <p className="text-center text-light-text-secondary dark:text-dark-text-secondary mb-6">
          Enter your new password below.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            type="password"
            label="New Password"
            value={newPassword}
            onChange={setNewPassword}
            required
            placeholder="Enter new password (min 8 characters)"
            showPasswordToggle
          />

          <Input
            type="password"
            label="Confirm Password"
            value={confirmPassword}
            onChange={setConfirmPassword}
            required
            placeholder="Confirm new password"
            showPasswordToggle
          />

          <Button type="submit" variant="primary" fullWidth loading={loading}>
            Reset Password
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
