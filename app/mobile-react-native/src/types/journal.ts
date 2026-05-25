export type CaptureMode = "dream" | "emotion" | "chat";

export type AuthAccount = {
  userId?: number;
  username?: string;
  nickname?: string;
  authorities?: string[];
};

export type QuickDreamCapture = {
  title?: string;
  content: string;
  dreamedAt?: string;
  tags?: string[];
};

export type EmotionCapture = {
  title?: string;
  content: string;
  recordedAt?: string;
  emotionTags?: string[];
};

