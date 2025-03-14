import { HttpEvent, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs';


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authToken = localStorage.getItem('token');

  const authCookie = req.clone({
    withCredentials: true
  })
  if (authToken) {
    const authReq = authCookie.clone({
      setHeaders: {
        Authorization: `Bearer ${authToken}`,
      },
    });
    return next(authReq);
  }
  return next(authCookie);
};

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    tap((event: HttpEvent<any>) => {
      if (event instanceof HttpResponse) {
        const token = event.headers.get('Authorization');
        if (token) {
          const newToken = token.replace('Bearer ', '');
          localStorage.setItem('token', newToken);
        }
      }
    })
  );
};
