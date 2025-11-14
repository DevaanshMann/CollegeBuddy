import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { apiClient } from "../../api/client";

type ProfileDto = {
    displayName: string;
    bio: string;
    avatarUrl: string;
    visibility: string;
};

const emptyProfile: ProfileDto = {
    displayName: "",
    bio: "",
    avatarUrl: "",
    visibility: "PUBLIC",
};

export function ProfilePage() {
    const [profile, setProfile] = useState<ProfileDto>(emptyProfile);
    const [loading, setLoading] = useState(true);
    const [loadedOnce, setLoadedOnce] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);

    // Get current user ID from JWT (you'll need to decode it or store it)
    // For now, we'll assume you have it stored somewhere
    const currentUserId = localStorage.getItem("currentUserId");

    useEffect(() => {
        void loadProfile();
    }, []);

    async function loadProfile() {
        setLoading(true);
        setError(null);
        setStatus(null);

        try {
            // Get profile - backend uses /profile/{userId}
            const data = await apiClient.get<ProfileDto>(`/profile/${currentUserId}`);
            if (data) {
                setProfile(data);
            } else {
                setProfile(emptyProfile);
            }
        } catch (err: any) {
            console.warn("Profile load error (treating as empty):", err);
            setProfile(emptyProfile);
        } finally {
            setLoadedOnce(true);
            setLoading(false);
        }
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setStatus(null);

        try {
            // Update profile - backend uses PUT /profile
            await apiClient.put("/profile", profile);
            setStatus("Profile saved!");
        } catch (err: any) {
            console.error("Save profile error:", err);
            setError(err.message ?? "Failed to save profile");
        }
    }

    if (loading && !loadedOnce) {
        return <p>Loading profile...</p>;
    }

    const isNew = !loadedOnce || profile.displayName === "";

    return (
        <div>
            <h2>Your Profile</h2>
            <p style={{ marginBottom: "1rem", color: "#9ca3af" }}>
                {isNew
                    ? "Set up your profile so other students can find you."
                    : "Update your profile information."}
            </p>

            <form
                onSubmit={handleSubmit}
                style={{ maxWidth: 480, display: "flex", flexDirection: "column", gap: "0.75rem" }}
            >
                <label>
                    Display Name *
                    <input
                        type="text"
                        value={profile.displayName}
                        onChange={(e) => setProfile({ ...profile, displayName: e.target.value })}
                        required
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                    />
                </label>

                <label>
                    Bio
                    <textarea
                        rows={3}
                        value={profile.bio}
                        onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                    />
                </label>

                <label>
                    Avatar URL (optional)
                    <input
                        type="url"
                        value={profile.avatarUrl}
                        onChange={(e) => setProfile({ ...profile, avatarUrl: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                    />
                </label>

                <label>
                    Visibility
                    <select
                        value={profile.visibility}
                        onChange={(e) => setProfile({ ...profile, visibility: e.target.value })}
                        style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
                    >
                        <option value="PUBLIC">Public (visible to campus)</option>
                        <option value="PRIVATE">Private (only me)</option>
                    </select>
                </label>

                <button type="submit" style={{ marginTop: "0.5rem", padding: "0.75rem" }}>
                    Save Profile
                </button>
            </form>

            {status && <p style={{ color: "#22c55e", marginTop: "0.75rem" }}>{status}</p>}
            {error && <p style={{ color: "red", marginTop: "0.75rem" }}>{error}</p>}
        </div>
    );
}