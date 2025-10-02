export interface TestUser {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: string;
  description: string;
  permissions: string[];
}

export const testUsers: TestUser[] = [
  {
    username: 'admin',
    email: 'admin@agrocloud.com',
    password: 'admin123',
    firstName: 'Admin',
    lastName: 'Sistema',
    role: 'SUPERADMIN',
    description: 'Administrador del sistema con acceso completo',
    permissions: ['Acceso total al sistema', 'Gestión de empresas', 'Gestión de usuarios', 'Dashboard global']
  },
  {
    username: 'admin_empresa',
    email: 'admin.empresa@agrocloud.com',
    password: 'admin123',
    firstName: 'Admin',
    lastName: 'Empresa',
    role: 'ADMINISTRADOR',
    description: 'Administrador de empresa con acceso a gestión completa',
    permissions: ['Gestión de usuarios de la empresa', 'Gestión de campos y lotes', 'Reportes financieros', 'Dashboard empresarial']
  },
  {
    username: 'admin.campo',
    email: 'admin.campo@agrocloud.com',
    password: 'admin123',
    firstName: 'Carlos',
    lastName: 'Administrador',
    role: 'ADMINISTRADOR',
    description: 'Administrador de campo con acceso a gestión operativa',
    permissions: ['Gestión de campos y lotes', 'Planificación de cultivos', 'Seguimiento de labores', 'Reportes de producción']
  },
  {
    username: 'tecnico.juan',
    email: 'tecnico.juan@agrocloud.com',
    password: 'admin123',
    firstName: 'Juan',
    lastName: 'Técnico',
    role: 'TECNICO',
    description: 'Técnico agrícola con acceso a gestión técnica',
    permissions: ['Gestión de insumos', 'Planificación de labores', 'Seguimiento de cultivos', 'Reportes técnicos']
  },
  {
    username: 'asesor.maria',
    email: 'asesor.maria@agrocloud.com',
    password: 'admin123',
    firstName: 'María',
    lastName: 'Asesora',
    role: 'ASESOR',
    description: 'Asesor agrícola con acceso a consultoría',
    permissions: ['Consulta de datos', 'Análisis de rendimientos', 'Recomendaciones técnicas', 'Reportes de asesoría']
  },
  {
    username: 'productor.pedro',
    email: 'productor.pedro@agrocloud.com',
    password: 'admin123',
    firstName: 'Pedro',
    lastName: 'Productor',
    role: 'PRODUCTOR',
    description: 'Productor con acceso a gestión de sus campos',
    permissions: ['Gestión de sus campos', 'Registro de labores', 'Seguimiento de cultivos', 'Reportes de producción']
  },
  {
    username: 'operario.luis',
    email: 'operario.luis@agrocloud.com',
    password: 'admin123',
    firstName: 'Luis',
    lastName: 'Operario',
    role: 'OPERARIO',
    description: 'Operario con acceso limitado a registro de labores',
    permissions: ['Registro de labores', 'Consulta de tareas asignadas', 'Reportes de trabajo realizado']
  },
  {
    username: 'invitado.ana',
    email: 'invitado.ana@agrocloud.com',
    password: 'admin123',
    firstName: 'Ana',
    lastName: 'Invitada',
    role: 'INVITADO',
    description: 'Usuario invitado con acceso de solo lectura',
    permissions: ['Consulta de datos', 'Visualización de reportes', 'Acceso limitado']
  }
];

export const getRoleColor = (role: string): string => {
  switch (role) {
    case 'SUPERADMIN':
      return 'bg-red-100 text-red-800 border-red-200';
    case 'ADMINISTRADOR':
      return 'bg-blue-100 text-blue-800 border-blue-200';
    case 'TECNICO':
      return 'bg-green-100 text-green-800 border-green-200';
    case 'ASESOR':
      return 'bg-purple-100 text-purple-800 border-purple-200';
    case 'PRODUCTOR':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'OPERARIO':
      return 'bg-orange-100 text-orange-800 border-orange-200';
    case 'INVITADO':
      return 'bg-gray-100 text-gray-800 border-gray-200';
    default:
      return 'bg-gray-100 text-gray-800 border-gray-200';
  }
};

export const getRoleIcon = (role: string): string => {
  switch (role) {
    case 'SUPERADMIN':
      return '👑';
    case 'ADMINISTRADOR':
      return '👨‍💼';
    case 'TECNICO':
      return '👨‍🔬';
    case 'ASESOR':
      return '👩‍💼';
    case 'PRODUCTOR':
      return '👨‍🌾';
    case 'OPERARIO':
      return '👷‍♂️';
    case 'INVITADO':
      return '👤';
    default:
      return '👤';
  }
};
