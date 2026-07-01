import {createHashRouter, RouteObject} from 'react-router-dom';
import Home from '@/app/(home)/page';
import BooksList from '@/app/books/page';
import BookDetail from '@/app/books/[id]/page';
import BookEditPage from '@/app/books/[id]/edit/page';
import BookRegister from '@/app/books/add/page';
import ReviewsList from '@/app/reviews/page';
import ReviewDetail from '@/app/reviews/[id]/page';
import SignUp from '@/app/auth/signup/page';
import Login from '@/app/auth/login/page';
import OAuthCallback from '@/app/auth/oauth-callback/page';
import AuthError from '@/app/auth/error/page';
import ClientLayout from "@/app/client-layout";
import NotFoundPage from "@/app/not-found"

const routes: RouteObject[] = [
  {
    path: '/',
    element: <ClientLayout/>,
    children: [
      {
        index: true,
        element: <Home/>,
      },
      {
        path: 'login',
        element: <Login/>,
      },
      {
        path: 'signup',
        element: <SignUp/>,
      },
      {
        path: 'oauth/callback',
        element: <OAuthCallback/>,
      },
      {
        path: 'auth/error',
        element: <AuthError/>,
      },
      {
        path: 'books',
        children: [
          {
            index: true,
            element: <BooksList/>,
          },
          {
            path: ':id',
            children: [
              {
                index: true,
                element: <BookDetail/>,
              },
              {
                path: 'edit',
                element: <BookEditPage/>
              }
            ]
          },
          {
            path: 'add',
            element: <BookRegister/>,
          },
        ],
      },
      {
        path: 'reviews',
        children: [
          {
            index: true,
            element: <ReviewsList/>,
          },
          {
            path: ':id',
            element: <ReviewDetail/>,
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage/>
  }
];

const router = createHashRouter(routes);

export default router;
