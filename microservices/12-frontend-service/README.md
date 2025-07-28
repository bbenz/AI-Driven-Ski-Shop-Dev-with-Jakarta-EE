# 12-frontend-service

This is the frontend service for the Ski Sales System, built with Next.js and TypeScript. It provides a user interface for customers to manage their ski materials, view information, and access AI support features.

## 🔧 Environment Setup

### Prerequisites
- Node.js 18+ 
- npm or yarn

### Environment Configuration

1. Copy the environment template:
```bash
cp .env.example .env.local
```

2. Edit `.env.local` with your configuration:
```bash
# Development settings
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_USE_API_GATEWAY=false
NEXT_PUBLIC_DISABLE_DUMMY_USER=false
```

**⚠️ Security Warning**: 
- Never commit `.env.local` to version control
- Set `NEXT_PUBLIC_DISABLE_DUMMY_USER=true` in production
- Use API Gateway (`NEXT_PUBLIC_USE_API_GATEWAY=true`) in production

## Getting Started

First, run the development server:

```bash
npx next dev --turbopack --port 3000
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Demo User (Development Only)

For testing purposes, a demo user is available in development mode:

- **Email**: `demo@skiresort.com`
- **Password**: `demo123`
- **Features**: Access to AI Support and all customer features

### Demo User Controls

- The demo user is automatically enabled in development mode
- Use the developer control panel (top-left corner) to toggle the demo user on/off
- Demo user is automatically disabled in production
- Set `NEXT_PUBLIC_DISABLE_DUMMY_USER=true` in your environment to disable manually

### Login Process

1. Go to `/login`
2. Use the demo credentials or click "デモアカウント情報を入力" button
3. Click login to access all features including AI Support

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
