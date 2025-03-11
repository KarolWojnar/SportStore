import { HttpInterceptorFn } from '@angular/common/http';


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
