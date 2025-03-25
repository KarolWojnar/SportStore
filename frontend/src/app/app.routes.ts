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
import { OrderComponent } from './payment/order/order.component';
import { OrderInfoComponent } from './profile/order-info/order-info.component';
import { OrdersComponent } from './profile/orders/orders.component';
import { OrdersComponent as AdminOrdersComponent } from './admin/orders/orders.component';
import { DetailsComponent as  ProfileDetailsComponent } from './profile/details/details.component';
import { EditComponent } from './profile/edit/edit.component';
import { UsersComponent } from './admin/users/users.component';
import { ProductsComponent } from './admin/products/products.component';
import { NewComponent } from './admin/products/new/new.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [NoAuthGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [NoAuthGuard] },
  { path: 'activate/:activationCode', component: ActivationComponent },
  { path: 'reset-password', component: RecoveryPasswordComponent, canActivate: [NoAuthGuard] },
  { path: 'reset-password/:resetCode', component: NewPasswordComponent, canActivate: [NoAuthGuard] },
  { path: 'products', component: ProductComponent, canActivate: [AuthGuard] },
  { path: 'products/:id', component: DetailsComponent },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', component: ProfileDetailsComponent },
      { path: 'edit', component: EditComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'orders/:id', component: OrderInfoComponent }
    ]
  },
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'checkout', component: PaymentComponent, canActivate: [AuthGuard] },
  { path: 'order', component: OrderComponent, canActivate: [AuthGuard] },
  {
    path: 'admin',
    component: AdminComponent, canActivate: [RoleGuard] ,
    children: [
      { path: 'users', component: UsersComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'products/new', component: NewComponent },
      { path: 'orders', component: AdminOrdersComponent }
    ]
  }
];
