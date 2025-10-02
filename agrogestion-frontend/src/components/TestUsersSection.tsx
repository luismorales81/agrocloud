import React, { useState } from 'react';
import { testUsers, getRoleColor, getRoleIcon, type TestUser } from '../data/testUsers';

interface TestUsersSectionProps {
  onUserSelect: (user: TestUser) => void;
}

const TestUsersSection: React.FC<TestUsersSectionProps> = ({ onUserSelect }) => {
  const [showUsers, setShowUsers] = useState(false);
  const [selectedRole, setSelectedRole] = useState<string>('all');

  const filteredUsers = selectedRole === 'all' 
    ? testUsers 
    : testUsers.filter(user => user.role === selectedRole);

  const uniqueRoles = Array.from(new Set(testUsers.map(user => user.role)));

  return (
    <div className="mt-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-800 flex items-center">
          <span className="mr-2">🧪</span>
          Usuarios de Prueba
        </h3>
        <button
          onClick={() => setShowUsers(!showUsers)}
          className="text-sm text-blue-600 hover:text-blue-800 font-medium"
        >
          {showUsers ? 'Ocultar' : 'Mostrar'} usuarios
        </button>
      </div>

      {showUsers && (
        <div className="space-y-4">
          {/* Filtro por rol */}
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setSelectedRole('all')}
              className={`px-3 py-1 rounded-full text-xs font-medium border ${
                selectedRole === 'all'
                  ? 'bg-blue-100 text-blue-800 border-blue-200'
                  : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
              }`}
            >
              Todos
            </button>
            {uniqueRoles.map(role => (
              <button
                key={role}
                onClick={() => setSelectedRole(role)}
                className={`px-3 py-1 rounded-full text-xs font-medium border ${
                  selectedRole === role
                    ? getRoleColor(role)
                    : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
                }`}
              >
                {getRoleIcon(role)} {role}
              </button>
            ))}
          </div>

          {/* Lista de usuarios */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {filteredUsers.map((user) => (
              <div
                key={user.username}
                className="bg-white p-3 rounded-lg border border-gray-200 hover:border-blue-300 transition-colors cursor-pointer"
                onClick={() => onUserSelect(user)}
              >
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center">
                    <span className="text-lg mr-2">{getRoleIcon(user.role)}</span>
                    <div>
                      <h4 className="font-medium text-gray-900">
                        {user.firstName} {user.lastName}
                      </h4>
                      <p className="text-sm text-gray-600">@{user.username}</p>
                    </div>
                  </div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium border ${getRoleColor(user.role)}`}>
                    {user.role}
                  </span>
                </div>
                
                <p className="text-sm text-gray-600 mb-2">{user.description}</p>
                
                <div className="text-xs text-gray-500">
                  <p><strong>Email:</strong> {user.email}</p>
                  <p><strong>Contraseña:</strong> {user.password}</p>
                </div>

                <div className="mt-2">
                  <p className="text-xs font-medium text-gray-700 mb-1">Permisos:</p>
                  <div className="flex flex-wrap gap-1">
                    {user.permissions.slice(0, 2).map((permission, index) => (
                      <span
                        key={index}
                        className="px-2 py-1 bg-gray-100 text-gray-600 rounded text-xs"
                      >
                        {permission}
                      </span>
                    ))}
                    {user.permissions.length > 2 && (
                      <span className="px-2 py-1 bg-gray-100 text-gray-600 rounded text-xs">
                        +{user.permissions.length - 2} más
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="text-xs text-gray-500 text-center pt-2 border-t border-gray-200">
            💡 Haz clic en cualquier usuario para autocompletar el formulario de login
          </div>
        </div>
      )}
    </div>
  );
};

export default TestUsersSection;
