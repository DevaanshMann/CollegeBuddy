import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { apiClient } from "../../api/client";

type ProfileResponse = {
    displayName: string | null;
    bio: string | null;
    avatarUrl: string | null;
    visibility: "PUBLIC" | "CAMPUS_ONLY" | "PRIVATE";
};

export function ProfilePage() {
    const [profile, setProfile] = useState<ProfileResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [message, setMessage] = useState<string | null>(null);

    useEffect(() => {
        async function loadProfile() {
            try {
                // If your endpoint is /profile (no /me), change this path
                const data = await apiClient.get<ProfileResponse>("/profile");
                setProfile(data);
            } catch (err: any) {
                setError(err.message ?? "Failed to load profile");
            } finally {
                setLoading(false);
            }
        }

        void loadProfile();
    }, []);

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        if (!profile) return;

        setSaving(true);
        setError(null);
        setMessage(null);

        try {
            await apiClient.put("/profile", profile);
            setMessage("Profile saved");
        } catch (err: any) {
            setError(err.message ?? "Failed to save profile");
        } finally {
            setSaving(false);
        }
    }

    if (loading) return <p>Loading profile...</p>;
    if (!profile) return <p>Profile not found.</p>;

    return (
        <div>
            <h2>Your Profile</h2>
            <form onSubmit={handleSubmit} style={{ maxWidth: 500, display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                <label>
                    Display Name
                    <input
                        type="text"
                        value={profile.displayName ?? ""}
                        onChange={(e) =>
                            setProfile({ ...profile, displayName: e.target.value })
                        }
                    />
                </label>

                <label>
                    Bio
                    <textarea
                        value={profile.bio ?? ""}
                        onChange={(e) =>
                            setProfile({ ...profile, bio: e.target.value })
                        }
                    />
                </label>

                <label>
                    Avatar URL
                    <input
                        type="text"
                        value={profile.avatarUrl ?? ""}
                        onChange={(e) =>
                            setProfile({ ...profile, avatarUrl: e.target.value })
                        }
                    />
                </label>

                <label>
                    Visibility
                    <select
                        value={profile.visibility}
                        onChange={(e) =>
                            setProfile({
                                ...profile,
                                visibility: e.target.value as ProfileResponse["visibility"],
                            })
                        }
                    >
                        <option value="PUBLIC">Public</option>
                        <option value="CAMPUS_ONLY">Campus only</option>
                        <option value="PRIVATE">Private</option>
                    </select>
                </label>

                <button type="submit" disabled={saving}>
                    {saving ? "Saving..." : "Save"}
                </button>
            </form>

            {message && <p style={{ color: "green" }}>{message}</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
}
