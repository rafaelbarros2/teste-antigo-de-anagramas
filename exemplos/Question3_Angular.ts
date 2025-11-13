/**
 * QUESTÃO 3: Angular - Features Core e Exemplo Prático
 * 
 * EXPERIÊNCIA COM ANGULAR:
 * 
 * Angular é um framework robusto para desenvolvimento de SPAs (Single Page Applications)
 * desenvolvido pelo Google. Trabalho com Angular desde a versão 8, atualmente usando v17+.
 * 
 * ============================================================================
 * CORE FEATURES DO ANGULAR:
 * ============================================================================
 * 
 * 1. ARQUITETURA BASEADA EM COMPONENTES
 *    - Componentes reutilizáveis com template, style e lógica encapsulados
 *    - Hierarquia de componentes (parent/child)
 *    - Lifecycle hooks (ngOnInit, ngOnDestroy, etc.)
 * 
 * 2. TWO-WAY DATA BINDING
 *    - [(ngModel)] para sincronização bidirecional
 *    - Property binding [property]
 *    - Event binding (event)
 * 
 * 3. DEPENDENCY INJECTION (DI)
 *    - Injeção de serviços via constructor
 *    - Providers hierárquicos (root, module, component)
 *    - Facilita testes e desacoplamento
 * 
 * 4. ROUTING
 *    - Sistema de rotas SPA
 *    - Lazy loading de módulos
 *    - Guards (CanActivate, CanDeactivate)
 *    - Resolvers para pré-carregamento de dados
 * 
 * 5. REACTIVE FORMS & TEMPLATE-DRIVEN FORMS
 *    - FormControl, FormGroup, FormBuilder
 *    - Validações síncronas e assíncronas
 *    - Custom validators
 * 
 * 6. RXJS & OBSERVABLES
 *    - Programação reativa
 *    - Operadores (map, filter, switchMap, catchError)
 *    - Gerenciamento de streams assíncronos
 * 
 * 7. HTTP CLIENT
 *    - Interceptors para auth, logging, error handling
 *    - Tipagem forte com TypeScript
 *    - Tratamento de erros centralizado
 * 
 * 8. DIRECTIVES & PIPES
 *    - Structural directives (*ngIf, *ngFor, *ngSwitch)
 *    - Attribute directives (ngClass, ngStyle)
 *    - Custom directives e pipes
 * 
 * 9. STANDALONE COMPONENTS (Angular 14+)
 *    - Componentes sem NgModule
 *    - Simplificação da estrutura
 * 
 * 10. SIGNALS (Angular 16+)
 *     - Reatividade granular
 *     - Alternativa moderna ao RxJS em alguns casos
 * 
 * ============================================================================
 * CASOS DE USO:
 * ============================================================================
 * 
 * - Aplicações enterprise de grande escala
 * - Dashboards administrativos complexos
 * - CRMs, ERPs, sistemas financeiros
 * - Aplicações com requisitos de segurança robustos
 * - Sistemas com múltiplos módulos/features
 * - Progressive Web Apps (PWA)
 * 
 * ============================================================================
 * EXEMPLO PRÁTICO: Sistema de Gestão de Pedidos (Order Management)
 * ============================================================================
 * 
 * Demonstra:
 * ✓ Comunicação entre componentes (Input/Output)
 * ✓ Data binding (property, event, two-way)
 * ✓ Integração com serviços
 * ✓ HTTP calls com tratamento de erro
 * ✓ RxJS operators
 * ✓ Reactive Forms
 */

// ============================================================================
// 1. MODEL (Domínio)
// ============================================================================

// src/app/models/order.model.ts
export interface Order {
  id: number;
  customerName: string;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  createdAt: Date;
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

// ============================================================================
// 2. SERVICE (Lógica de Negócio e HTTP)
// ============================================================================

// src/app/services/order.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, catchError, tap, retry } from 'rxjs/operators';

@Injectable({
  providedIn: 'root' // Singleton em toda aplicação
})
export class OrderService {
  private apiUrl = 'https://api.example.com/orders';
  
  // BehaviorSubject para compartilhar estado entre componentes
  private ordersSubject = new BehaviorSubject<Order[]>([]);
  public orders$ = this.ordersSubject.asObservable();
  
  constructor(private http: HttpClient) {}
  
  /**
   * FEATURE 1: HTTP CLIENT com RxJS Operators
   * Busca todos os pedidos com tratamento de erro e retry
   */
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl).pipe(
      retry(2), // Retenta até 2 vezes em caso de erro
      map(orders => orders.map(order => ({
        ...order,
        createdAt: new Date(order.createdAt) // Converte string para Date
      }))),
      tap(orders => this.ordersSubject.next(orders)), // Atualiza BehaviorSubject
      catchError(this.handleError)
    );
  }
  
  /**
   * Busca pedido específico por ID
   */
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }
  
  /**
   * FEATURE 2: POST com validação
   * Cria novo pedido
   */
  createOrder(order: Omit<Order, 'id'>): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order).pipe(
      tap(newOrder => {
        const currentOrders = this.ordersSubject.value;
        this.ordersSubject.next([...currentOrders, newOrder]);
      }),
      catchError(this.handleError)
    );
  }
  
  /**
   * FEATURE 3: UPDATE com otimistic update
   * Atualiza status do pedido
   */
  updateOrderStatus(orderId: number, status: OrderStatus): Observable<Order> {
    return this.http.patch<Order>(`${this.apiUrl}/${orderId}/status`, { status }).pipe(
      tap(updatedOrder => {
        const orders = this.ordersSubject.value.map(order =>
          order.id === orderId ? updatedOrder : order
        );
        this.ordersSubject.next(orders);
      }),
      catchError(this.handleError)
    );
  }
  
  /**
   * Tratamento centralizado de erros HTTP
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Erro desconhecido';
    
    if (error.error instanceof ErrorEvent) {
      // Erro do lado do cliente
      errorMessage = `Erro: ${error.error.message}`;
    } else {
      // Erro do lado do servidor
      errorMessage = `Código: ${error.status}\nMensagem: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}

// ============================================================================
// 3. PARENT COMPONENT (Lista de Pedidos)
// ============================================================================

// src/app/components/order-list/order-list.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { OrderService } from '../../services/order.service';
import { Order, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss']
})
export class OrderListComponent implements OnInit, OnDestroy {
  orders: Order[] = [];
  selectedOrder: Order | null = null;
  loading = false;
  error: string | null = null;
  
  // Subject para gerenciar unsubscribe (evita memory leaks)
  private destroy$ = new Subject<void>();
  
  constructor(private orderService: OrderService) {}
  
  ngOnInit(): void {
    this.loadOrders();
  }
  
  ngOnDestroy(): void {
    // Cleanup: cancela todas as subscrições
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  /**
   * FEATURE: Subscription com takeUntil para evitar memory leak
   */
  loadOrders(): void {
    this.loading = true;
    this.error = null;
    
    this.orderService.getOrders()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (orders) => {
          this.orders = orders;
          this.loading = false;
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
        }
      });
  }
  
  /**
   * FEATURE: EVENT HANDLER - Recebe evento do componente filho
   * Demonstra comunicação child → parent via @Output
   */
  onOrderSelected(order: Order): void {
    this.selectedOrder = order;
    console.log('Pedido selecionado:', order);
  }
  
  /**
   * FEATURE: Atualização de status
   */
  onStatusChanged(orderId: number, newStatus: OrderStatus): void {
    this.orderService.updateOrderStatus(orderId, newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedOrder) => {
          console.log('Status atualizado:', updatedOrder);
          // Lista é atualizada automaticamente via BehaviorSubject no service
        },
        error: (err) => {
          this.error = `Erro ao atualizar: ${err.message}`;
        }
      });
  }
  
  /**
   * Filtro de pedidos por status (usado no template)
   */
  filterByStatus(status: OrderStatus): Order[] {
    return this.orders.filter(order => order.status === status);
  }
}

// src/app/components/order-list/order-list.component.html
/**
<!-- TEMPLATE com Data Binding -->
<div class="order-list-container">
  <h2>Gestão de Pedidos</h2>
  
  <!-- LOADING STATE -->
  <div *ngIf="loading" class="loading-spinner">
    Carregando pedidos...
  </div>
  
  <!-- ERROR STATE -->
  <div *ngIf="error" class="error-message">
    {{ error }}
    <button (click)="loadOrders()">Tentar Novamente</button>
  </div>
  
  <!-- SUCCESS STATE -->
  <div *ngIf="!loading && !error">
    <!-- PROPERTY BINDING + EVENT BINDING -->
    <app-order-filter
      [totalOrders]="orders.length"
      (filterChanged)="onFilterChanged($event)">
    </app-order-filter>
    
    <!-- STRUCTURAL DIRECTIVE: *ngFor -->
    <div class="orders-grid">
      <app-order-card
        *ngFor="let order of orders; trackBy: trackByOrderId"
        [order]="order"
        [selected]="selectedOrder?.id === order.id"
        (orderClick)="onOrderSelected(order)"
        (statusChange)="onStatusChanged(order.id, $event)">
      </app-order-card>
    </div>
    
    <!-- CONDITIONAL RENDERING: *ngIf -->
    <div *ngIf="orders.length === 0" class="empty-state">
      <p>Nenhum pedido encontrado</p>
    </div>
  </div>
  
  <!-- COMPONENTE FILHO com PROPERTY BINDING -->
  <app-order-details
    *ngIf="selectedOrder"
    [order]="selectedOrder"
    (close)="selectedOrder = null">
  </app-order-details>
</div>
 */

// ============================================================================
// 4. CHILD COMPONENT (Card de Pedido)
// ============================================================================

// src/app/components/order-card/order-card.component.ts
import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { Order, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-card',
  templateUrl: './order-card.component.html',
  styleUrls: ['./order-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush // Otimização de performance
})
export class OrderCardComponent {
  /**
   * FEATURE: @Input - Recebe dados do componente pai
   * Comunicação parent → child
   */
  @Input() order!: Order;
  @Input() selected = false;
  
  /**
   * FEATURE: @Output - Emite eventos para o componente pai
   * Comunicação child → parent
   */
  @Output() orderClick = new EventEmitter<Order>();
  @Output() statusChange = new EventEmitter<OrderStatus>();
  
  // Enum disponível no template
  OrderStatus = OrderStatus;
  
  /**
   * Emite evento de clique
   */
  onClick(): void {
    this.orderClick.emit(this.order);
  }
  
  /**
   * Emite evento de mudança de status
   */
  onStatusUpdate(newStatus: OrderStatus): void {
    if (newStatus !== this.order.status) {
      this.statusChange.emit(newStatus);
    }
  }
  
  /**
   * Método auxiliar para estilo condicional
   */
  getStatusClass(): string {
    return `status-${this.order.status.toLowerCase()}`;
  }
  
  /**
   * Formata valor monetário (pode ser substituído por pipe)
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  }
}

// src/app/components/order-card/order-card.component.html
/**
<!-- TEMPLATE do Card -->
<div 
  class="order-card" 
  [class.selected]="selected"
  [ngClass]="getStatusClass()"
  (click)="onClick()">
  
  <div class="card-header">
    <h3>Pedido #{{ order.id }}</h3>
    <!-- PIPE para data -->
    <span class="date">{{ order.createdAt | date:'dd/MM/yyyy HH:mm' }}</span>
  </div>
  
  <div class="card-body">
    <p><strong>Cliente:</strong> {{ order.customerName }}</p>
    <p><strong>Itens:</strong> {{ order.items.length }}</p>
    <!-- CUSTOM PIPE -->
    <p><strong>Total:</strong> {{ order.totalAmount | currency:'BRL':'symbol':'1.2-2' }}</p>
  </div>
  
  <div class="card-footer">
    <!-- TWO-WAY BINDING com ngModel -->
    <select 
      [(ngModel)]="order.status" 
      (change)="onStatusUpdate(order.status)"
      (click)="$event.stopPropagation()">
      <option [value]="OrderStatus.PENDING">Pendente</option>
      <option [value]="OrderStatus.CONFIRMED">Confirmado</option>
      <option [value]="OrderStatus.SHIPPED">Enviado</option>
      <option [value]="OrderStatus.DELIVERED">Entregue</option>
      <option [value]="OrderStatus.CANCELLED">Cancelado</option>
    </select>
  </div>
</div>
 */

/**
 * ============================================================================
 * RESUMO DAS FEATURES DEMONSTRADAS:
 * ============================================================================
 * 
 * ✓ Component Communication:
 *   - @Input (parent → child)
 *   - @Output + EventEmitter (child → parent)
 *   - Service com BehaviorSubject (sibling communication)
 * 
 * ✓ Data Binding:
 *   - Property binding: [property]="value"
 *   - Event binding: (event)="handler()"
 *   - Two-way binding: [(ngModel)]="property"
 *   - Class binding: [class.active]="isActive"
 *   - Style binding: [style.color]="color"
 * 
 * ✓ Service Integration:
 *   - Dependency Injection
 *   - HttpClient
 *   - RxJS operators (map, tap, catchError, retry, takeUntil)
 *   - Shared state com BehaviorSubject
 * 
 * ✓ Lifecycle Hooks:
 *   - ngOnInit (inicialização)
 *   - ngOnDestroy (cleanup)
 * 
 * ✓ Directives:
 *   - *ngIf, *ngFor, *ngSwitch
 *   - ngClass, ngStyle
 * 
 * ✓ Pipes:
 *   - Built-in: date, currency
 *   - Custom pipes possíveis
 * 
 * ✓ Performance:
 *   - ChangeDetectionStrategy.OnPush
 *   - trackBy em *ngFor
 *   - Unsubscribe com takeUntil
 */
