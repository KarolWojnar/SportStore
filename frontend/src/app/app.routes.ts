import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ActivationComponent } from './auth/activation/activation.component';
import { ProductComponent } from './product/product.component';
import { ProfileComponent } from './profile/profile.component';
import { AuthGuard } from './guard/auth.guard';
import { RoleGuard } from './guard/role.guard';
import { AdminComponent } from './admin/admin.component';
import { CartComponent } from './cart/cart.component';
import { NoAuthGuard } from './guard/no-auth.guard';
import { DetailsComponent } from './product/details/details.component';
import { RecoveryPasswordComponent } from './auth/recovery-password/recovery-password.component';
import { NewPasswordComponent } from './auth/new-password/new-password.component';
import { PaymentComponent } from './payment/payment.component';
import { ProcessComponent } from './payment/process/process.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [NoAuthGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [NoAuthGuard] },
  { path: 'activate/:activationCode', component: ActivationComponent },
  { path: 'reset-password', component: RecoveryPasswordComponent, canActivate: [NoAuthGuard] },
  { path: 'reset-password/:resetCode', component: NewPasswordComponent, canActivate: [NoAuthGuard] },
  { path: 'products', component: ProductComponent, canActivate: [AuthGuard] },
  { path: 'products/:id', component: DetailsComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'checkout', component: PaymentComponent, canActivate: [AuthGuard] },
  { path: 'payment', component: ProcessComponent, canActivate: [AuthGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [RoleGuard] }
];
