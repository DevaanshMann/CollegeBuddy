import { useEffect, useState } from "react";
import type { FormEvent, ChangeEvent } from "react";
import { apiClient } from "../../api/client";
import { JWT_STORAGE_KEY, API_BASE_URL } from "../../config";

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
    const [editedProfile, setEditedProfile] = useState<ProfileDto>(emptyProfile);
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    // Helper to get full image URL
    const getAvatarUrl = (url: string | null | undefined): string | null => {
        if (!url) return null;

        console.log("Processing avatar URL:", url); // Debug

        // If it's already a full URL, return as-is
        if (url.startsWith('http://') || url.startsWith('https://')) {
            return url;
        }

        // If it's a relative path, prepend the API base URL
        if (url.startsWith('/')) {
            const fullUrl = `${API_BASE_URL}${url}`;
            console.log("Converted to full URL:", fullUrl); // Debug
            return fullUrl;
        }

        return url;
    };

    // Decode JWT to get user ID
    const getUserIdFromToken = () => {
        const token = localStorage.getItem(JWT_STORAGE_KEY);
        if (!token) return null;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.sub;
        } catch (e) {
            console.error("Failed to decode token:", e);
            return null;
        }
    };

    useEffect(() => {
        void loadProfile();
    }, []);

    async function loadProfile() {
        setLoading(true);
        setError(null);
        setStatus(null);

        const userId = getUserIdFromToken();
        if (!userId) {
            setError("Not authenticated");
            setLoading(false);
            return;
        }

        try {
            const data = await apiClient.get<ProfileDto>(`/profile/${userId}`);
            console.log("Loaded profile:", data); // Debug
            setProfile(data);
            setEditedProfile(data);
        } catch (err: any) {
            console.warn("Profile not found, showing empty profile:", err);
            setProfile(emptyProfile);
            setEditedProfile(emptyProfile);
            setIsEditing(true);
        } finally {
            setLoading(false);
        }
    }

    async function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0];
        if (!file) return;

        // Validate file type
        if (!file.type.startsWith('image/')) {
            setError("Please select an image file");
            return;
        }

        // Validate file size (5MB)
        if (file.size > 5 * 1024 * 1024) {
            setError("File size must be less than 5MB");
            return;
        }

        setSelectedFile(file);

        // Create preview
        const reader = new FileReader();
        reader.onloadend = () => {
            setPreviewUrl(reader.result as string);
        };
        reader.readAsDataURL(file);

        setError(null);
    }

    async function handleUploadAvatar() {
        if (!selectedFile) return;

        setUploading(true);
        setError(null);

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);

            const token = localStorage.getItem(JWT_STORAGE_KEY);
            const response = await fetch(`${API_BASE_URL}/profile/upload-avatar`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Upload failed:", errorText);
                throw new Error(`Upload failed: ${response.status}`);
            }

            const data = await response.json();
            console.log("Upload response from backend:", data);

            // Backend returns relative path like /uploads/avatars/123_abc.jpg
            // Store ONLY the relative path (NOT the full URL)
            const avatarUrl = data.avatarUrl;
            console.log("Storing relative avatar URL:", avatarUrl);

            // Update the edited profile with the relative path
            setEditedProfile({ ...editedProfile, avatarUrl: avatarUrl });

            // Also update the main profile so preview works
            setProfile({ ...profile, avatarUrl: avatarUrl });

            setSelectedFile(null);
            setPreviewUrl(null);
            setStatus("Avatar uploaded successfully! Click 'Update Profile' to save.");
        } catch (err: any) {
            console.error("Upload error:", err);
            setError(err.message ?? "Failed to upload avatar");
        } finally {
            setUploading(false);
        }
    }

    async function handleSave(e: FormEvent) {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setStatus(null);

        try {
            console.log("Saving profile with avatar URL:", editedProfile.avatarUrl); // Debug
            await apiClient.put("/profile", editedProfile);
            setProfile(editedProfile);
            setIsEditing(false);
            setStatus("Profile saved successfully!");
        } catch (err: any) {
            console.error("Save profile error:", err);
            setError(err.message ?? "Failed to save profile");
        } finally {
            setSaving(false);
        }
    }

    function handleEdit() {
        setEditedProfile(profile);
        setIsEditing(true);
        setError(null);
        setStatus(null);
        setSelectedFile(null);
        setPreviewUrl(null);
    }

    function handleCancel() {
        setEditedProfile(profile);
        setIsEditing(false);
        setError(null);
        setStatus(null);
        setSelectedFile(null);
        setPreviewUrl(null);
    }

    if (loading) {
        return (
            <div style={{ textAlign: "center", padding: "2rem" }}>
                <p>Loading profile...</p>
            </div>
        );
    }

    // View Mode
    if (!isEditing) {
        const avatarUrl = profile.avatarUrl
            ? (profile.avatarUrl.startsWith('http')
                ? profile.avatarUrl
                : `${API_BASE_URL}${profile.avatarUrl}`)
            : null;

        console.log("=== VIEW MODE DEBUG ===");
        console.log("Raw avatarUrl from profile:", profile.avatarUrl);
        console.log("Computed full avatarUrl:", avatarUrl);
        console.log("API_BASE_URL:", API_BASE_URL);

        return (
            <div style={{ maxWidth: "600px", margin: "0 auto" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "2rem" }}>
                    <h2>Your Profile</h2>
                    <button onClick={handleEdit} style={{ padding: "0.5rem 1rem" }}>
                        Edit Profile
                    </button>
                </div>

                {status && <p style={{ color: "#22c55e", marginBottom: "1rem" }}>{status}</p>}

                <div style={{
                    border: "1px solid #ddd",
                    borderRadius: "8px",
                    padding: "2rem",
                    backgroundColor: "#f9f9f9"
                }}>
                    {/* AVATAR SECTION - MUST ALWAYS SHOW */}
                    <div style={{
                        textAlign: "center",
                        marginBottom: "2rem",
                        padding: "1rem",
                        backgroundColor: "#fff",
                        borderRadius: "8px"
                    }}>
                        {avatarUrl ? (
                            <div>
                                <img
                                    src={avatarUrl}
                                    alt="Profile avatar"
                                    crossOrigin="anonymous"  // Add this line
                                    style={{
                                        width: "120px",
                                        height: "120px",
                                        borderRadius: "50%",
                                        objectFit: "cover",
                                        border: "3px solid #007bff",
                                        display: "block",
                                        margin: "0 auto"
                                    }}
                                    onLoad={() => console.log("✅ Avatar loaded successfully!")}
                                    onError={(e) => {
                                        console.error("❌ Avatar failed to load from:", avatarUrl);
                                        e.currentTarget.style.display = 'none';
                                        const fallback = e.currentTarget.nextElementSibling as HTMLElement;
                                        if (fallback) fallback.style.display = 'flex';
                                    }}
                                />
                                {/* Fallback avatar (hidden by default, shown on error) */}
                                <div
                                    style={{
                                        width: "120px",
                                        height: "120px",
                                        borderRadius: "50%",
                                        backgroundColor: "#007bff",
                                        color: "white",
                                        display: "none",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        fontSize: "3rem",
                                        fontWeight: "bold",
                                        margin: "0 auto"
                                    }}
                                >
                                    {profile.displayName?.charAt(0).toUpperCase() || "?"}
                                </div>
                            </div>
                        ) : (
                            <div
                                style={{
                                    width: "120px",
                                    height: "120px",
                                    borderRadius: "50%",
                                    backgroundColor: "#007bff",
                                    color: "white",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    fontSize: "3rem",
                                    fontWeight: "bold",
                                    margin: "0 auto"
                                }}
                            >
                                {profile.displayName?.charAt(0).toUpperCase() || "?"}
                            </div>
                        )}
                        <p style={{ marginTop: "0.5rem", fontSize: "0.85rem", color: "#666" }}>
                            {avatarUrl ? "Profile Picture" : "No profile picture"}
                        </p>
                    </div>

                    <div style={{ marginBottom: "1.5rem" }}>
                        <label style={{ fontWeight: "bold", color: "#666", fontSize: "0.9rem" }}>
                            Display Name
                        </label>
                        <p style={{ fontSize: "1.5rem", margin: "0.25rem 0 0 0" }}>
                            {profile.displayName || <em style={{ color: "#999" }}>Not set</em>}
                        </p>
                    </div>

                    <div style={{ marginBottom: "1.5rem" }}>
                        <label style={{ fontWeight: "bold", color: "#666", fontSize: "0.9rem" }}>
                            Bio
                        </label>
                        <p style={{ margin: "0.25rem 0 0 0", lineHeight: "1.6" }}>
                            {profile.bio || <em style={{ color: "#999" }}>No bio added</em>}
                        </p>
                    </div>

                    <div style={{ marginBottom: "1.5rem" }}>
                        <label style={{ fontWeight: "bold", color: "#666", fontSize: "0.9rem" }}>
                            Visibility
                        </label>
                        <p style={{ margin: "0.25rem 0 0 0" }}>
                        <span style={{
                            display: "inline-block",
                            padding: "0.25rem 0.75rem",
                            borderRadius: "12px",
                            backgroundColor: profile.visibility === "PUBLIC" ? "#e3f2fd" : "#fff3cd",
                            color: profile.visibility === "PUBLIC" ? "#1976d2" : "#856404",
                            fontSize: "0.9rem"
                        }}>
                            {profile.visibility === "PUBLIC" ? "🌍 Public (visible to campus)" : "🔒 Private (only me)"}
                        </span>
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    // Edit Mode
    const editAvatarUrl = getAvatarUrl(editedProfile.avatarUrl);

    return (
        <div style={{ maxWidth: "600px", margin: "0 auto" }}>
            <h2>Edit Your Profile</h2>
            <p style={{ marginBottom: "2rem", color: "#666" }}>
                Update your information below. Changes will be visible to other students based on your visibility settings.
            </p>

            {status && <p style={{ color: "#22c55e", marginBottom: "1rem" }}>{status}</p>}
            {error && <p style={{ color: "red", marginBottom: "1rem" }}>{error}</p>}

            <form
                onSubmit={handleSave}
                style={{ display: "flex", flexDirection: "column", gap: "1.25rem" }}
            >
                {/* Avatar Upload Section */}
                <div style={{
                    border: "2px dashed #ddd",
                    borderRadius: "8px",
                    padding: "1.5rem",
                    backgroundColor: "#f9f9f9"
                }}>
                    <label style={{ fontWeight: "bold", display: "block", marginBottom: "0.5rem" }}>
                        Profile Picture
                    </label>

                    <div style={{ display: "flex", alignItems: "center", gap: "1.5rem", marginBottom: "1rem" }}>
                        {/* Current/Preview Avatar */}
                        <div>
                            {previewUrl ? (
                                <img
                                    src={previewUrl}
                                    alt="Avatar preview"
                                    style={{
                                        width: "80px",
                                        height: "80px",
                                        borderRadius: "50%",
                                        objectFit: "cover",
                                        border: "2px solid #007bff"
                                    }}
                                />
                            ) : editAvatarUrl ? (
                                <img
                                    src={editAvatarUrl}
                                    alt="Current avatar"
                                    crossOrigin="anonymous"
                                    style={{
                                        width: "80px",
                                        height: "80px",
                                        borderRadius: "50%",
                                        objectFit: "cover",
                                        border: "2px solid #007bff"
                                    }}
                                    onError={(e) => {
                                        console.error("Failed to load avatar in edit mode from:", editAvatarUrl);
                                        e.currentTarget.style.display = 'none';
                                    }}
                                />
                            ) : (
                                <div
                                    style={{
                                        width: "80px",
                                        height: "80px",
                                        borderRadius: "50%",
                                        backgroundColor: "#007bff",
                                        color: "white",
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        fontSize: "2rem",
                                        fontWeight: "bold"
                                    }}
                                >
                                    {editedProfile.displayName?.charAt(0).toUpperCase() || "?"}
                                </div>
                            )}
                        </div>

                        {/* Upload Controls */}
                        <div style={{ flex: 1 }}>
                            <input
                                type="file"
                                accept="image/*"
                                onChange={handleFileChange}
                                style={{ marginBottom: "0.5rem" }}
                            />
                            {selectedFile && (
                                <button
                                    type="button"
                                    onClick={handleUploadAvatar}
                                    disabled={uploading}
                                    style={{
                                        padding: "0.5rem 1rem",
                                        backgroundColor: "#28a745",
                                        color: "white",
                                        border: "none",
                                        borderRadius: "4px",
                                        cursor: uploading ? "not-allowed" : "pointer",
                                        opacity: uploading ? 0.6 : 1
                                    }}
                                >
                                    {uploading ? "Uploading..." : "Upload Avatar"}
                                </button>
                            )}
                        </div>
                    </div>

                    <small style={{ color: "#666", fontSize: "0.85rem" }}>
                        Accepted: JPG, PNG, GIF (max 5MB)
                    </small>
                </div>

                <div>
                    <label style={{ fontWeight: "bold", display: "block", marginBottom: "0.5rem" }}>
                        Display Name *
                    </label>
                    <input
                        type="text"
                        value={editedProfile.displayName}
                        onChange={(e) => setEditedProfile({ ...editedProfile, displayName: e.target.value })}
                        required
                        placeholder="Enter your display name"
                        style={{ width: "100%", padding: "0.75rem", fontSize: "1rem" }}
                    />
                </div>

                <div>
                    <label style={{ fontWeight: "bold", display: "block", marginBottom: "0.5rem" }}>
                        Bio
                    </label>
                    <textarea
                        rows={4}
                        value={editedProfile.bio}
                        onChange={(e) => setEditedProfile({ ...editedProfile, bio: e.target.value })}
                        placeholder="Tell others about yourself..."
                        style={{ width: "100%", padding: "0.75rem", fontSize: "1rem", resize: "vertical" }}
                    />
                    <small style={{ color: "#666", fontSize: "0.85rem" }}>
                        {editedProfile.bio?.length || 0} / 500 characters
                    </small>
                </div>

                <div>
                    <label style={{ fontWeight: "bold", display: "block", marginBottom: "0.5rem" }}>
                        Visibility
                    </label>
                    <select
                        value={editedProfile.visibility}
                        onChange={(e) => setEditedProfile({ ...editedProfile, visibility: e.target.value })}
                        style={{ width: "100%", padding: "0.75rem", fontSize: "1rem" }}
                    >
                        <option value="PUBLIC">🌍 Public (visible to your campus)</option>
                        <option value="PRIVATE">🔒 Private (only visible to you)</option>
                    </select>
                    <small style={{ color: "#666", fontSize: "0.85rem", display: "block", marginTop: "0.5rem" }}>
                        {editedProfile.visibility === "PUBLIC"
                            ? "Other students at your campus can find and view your profile"
                            : "Your profile will be hidden from search results"}
                    </small>
                </div>

                <div style={{ display: "flex", gap: "1rem", marginTop: "1rem" }}>
                    <button
                        type="submit"
                        disabled={saving}
                        style={{
                            flex: 1,
                            padding: "0.75rem",
                            backgroundColor: "#007bff",
                            color: "white",
                            border: "none",
                            borderRadius: "4px",
                            fontSize: "1rem",
                            cursor: saving ? "not-allowed" : "pointer",
                            opacity: saving ? 0.6 : 1
                        }}
                    >
                        {saving ? "Saving..." : "Update Profile"}
                    </button>
                    <button
                        type="button"
                        onClick={handleCancel}
                        disabled={saving}
                        style={{
                            flex: 1,
                            padding: "0.75rem",
                            backgroundColor: "#6c757d",
                            color: "white",
                            border: "none",
                            borderRadius: "4px",
                            fontSize: "1rem",
                            cursor: saving ? "not-allowed" : "pointer",
                            opacity: saving ? 0.6 : 1
                        }}
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}