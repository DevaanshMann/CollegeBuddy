import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { apiClient } from "../../api/client";

type Visibility = "PUBLIC_CAMPUS" | "FRIENDS_ONLY" | "HIDDEN";

type ProfileDto = {
    displayName: string;
    bio: string;
    avatarUrl: string;
    visibility: Visibility;
};

const emptyProfile: ProfileDto = {
    displayName: "",
    bio: "",
    avatarUrl: "",
    visibility: "PUBLIC_CAMPUS",
};

export function ProfilePage() {
    const [profile, setProfile] = useState<ProfileDto>(emptyProfile);
    const [loading, setLoading] = useState(true);
    const [loadedOnce, setLoadedOnce] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);

    useEffect(() => {
        void loadProfile();
    }, []);

    async function loadProfile() {
        setLoading(true);
        setError(null);
        setStatus(null);

        try {
            const data = await apiClient.get<any>("/profile/me");
            if (data) {
                setProfile({
                    displayName: data.displayName ?? "",
                    bio: data.bio ?? "",
                    avatarUrl: data.avatarUrl ?? "",
                    visibility: (data.visibility as Visibility) ?? "PUBLIC_CAMPUS",
                });
            } else {
                setProfile(emptyProfile);
            }
        } catch (err: any) {
            // Treat "not found" as "time to create a profile"
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
            await apiClient.put("/profile/me", profile);
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
                    ? "You haven't set up your profile yet. This is what other students will see."
                    : "Update your profile. Your visibility controls how classmates find you."}
            </p>

            <form
                onSubmit={handleSubmit}
                style={{ maxWidth: 480, display: "flex", flexDirection: "column", gap: "0.75rem" }}
            >
                <label>
                    Display Name
                    <input
                        type="text"
                        value={profile.displayName}
                        onChange={(e) => setProfile({ ...profile, displayName: e.target.value })}
                        required
                    />
                </label>

                <label>
                    Bio
                    <textarea
                        rows={3}
                        value={profile.bio}
                        onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
                    />
                </label>

                <label>
                    Avatar URL (optional)
                    <input
                        type="url"
                        value={profile.avatarUrl}
                        onChange={(e) => setProfile({ ...profile, avatarUrl: e.target.value })}
                    />
                </label>

                <label>
                    Visibility
                    <select
                        value={profile.visibility}
                        onChange={(e) =>
                            setProfile({ ...profile, visibility: e.target.value as Visibility })
                        }
                    >
                        <option value="PUBLIC_CAMPUS">Public to your campus</option>
                        <option value="FRIENDS_ONLY">Visible to friends only</option>
                        <option value="HIDDEN">Hidden from search</option>
                    </select>
                </label>

                <button type="submit" style={{ marginTop: "0.5rem" }}>
                    Save profile
                </button>
            </form>

            {status && <p style={{ color: "#22c55e", marginTop: "0.75rem" }}>{status}</p>}
            {error && <p style={{ color: "red", marginTop: "0.75rem" }}>{error}</p>}
        </div>
    );
}
