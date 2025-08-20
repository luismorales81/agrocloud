import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';

interface Location {
	lat: number;
	lng: number;
	accuracy?: number;
	timestamp?: number;
	locality?: string; // Nombre de la localidad
}

interface LocationContextType {
	location: Location | null;
	loading: boolean;
	error: string | null;
	requestLocation: () => void;
}

const LocationContext = createContext<LocationContextType | undefined>(undefined);

export const useLocation = () => {
	const context = useContext(LocationContext);
	if (context === undefined) {
		throw new Error('useLocation must be used within a LocationProvider');
	}
	return context;
};

interface LocationProviderProps {
	children: ReactNode;
}

// Geocodificación inversa con múltiples proveedores (Google → BigDataCloud → OpenStreetMap)
const getLocalityFromCoordinates = async (lat: number, lng: number): Promise<string> => {
	// 1) Google Geocoding API (requiere API habilitada y facturación)
	try {
		const googleResp = await fetch(
			`https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=AIzaSyCWz9FKCHBdLqbjhHCPcECww5hs2ugiWA0&language=es`
		);
		if (googleResp.ok) {
			const data = await googleResp.json();
			if (data.results && data.results.length > 0) {
				const result = data.results[0];
				for (const component of result.address_components) {
					if (
						component.types.includes('locality') ||
						component.types.includes('sublocality') ||
						component.types.includes('administrative_area_level_2') ||
						component.types.includes('administrative_area_level_1')
					) {
						return component.long_name;
					}
				}
				return (result.formatted_address || '').split(',')[0] || 'Ubicación desconocida';
			}
		}
	} catch (e) {
		console.warn('Google Geocoding falló, probando fallback...', e);
	}

	// 2) BigDataCloud (gratis, sin API key)
	try {
		const bdcResp = await fetch(
			`https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${lat}&longitude=${lng}&localityLanguage=es`
		);
		if (bdcResp.ok) {
			const data = await bdcResp.json();
			// Intentar múltiples campos
			const directLocality = data.locality || data.city || data.cityLocality || data.principalSubdivision;
			if (directLocality) return directLocality as string;
			// Explorar localityInfo.administrative en busca de un nombre razonable
			if (data.localityInfo && Array.isArray(data.localityInfo.administrative)) {
				const admin = data.localityInfo.administrative as Array<{ order?: number; name?: string }>;
				// Preferir niveles medios (ciudad/municipio) si existe
				const byOrder = admin
					.filter((a) => !!a && !!a.name)
					.sort((a, b) => (Number(a.order ?? 99) - Number(b.order ?? 99)));
				if (byOrder.length > 0 && byOrder[0].name) return byOrder[0].name;
			}
		}
	} catch (e) {
		console.warn('BigDataCloud falló, probando OSM...', e);
	}

	// 3) OpenStreetMap Nominatim
	try {
		const osmResp = await fetch(
			`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&zoom=10&accept-language=es`
		);
		if (osmResp.ok) {
			const data = await osmResp.json();
			const addr = data.address || {};
			const locality = addr.city || addr.town || addr.village || addr.municipality || addr.county || addr.state;
			if (locality) return locality as string;
		}
	} catch (e) {
		console.warn('OSM Nominatim falló también.', e);
	}

	return 'Ubicación desconocida';
};

export const LocationProvider: React.FC<LocationProviderProps> = ({ children }) => {
	const [location, setLocation] = useState<Location | null>(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState<string | null>(null);

	const requestLocation = () => {
		if (!navigator.geolocation) {
			setError('Geolocalización no soportada en este navegador');
			return;
		}

		setLoading(true);
		setError(null);

		navigator.geolocation.getCurrentPosition(
			async (position) => {
				const newLocation: Location = {
					lat: position.coords.latitude,
					lng: position.coords.longitude,
					accuracy: position.coords.accuracy,
					timestamp: position.timestamp
				};

				try {
					newLocation.locality = await getLocalityFromCoordinates(newLocation.lat, newLocation.lng);
				} catch (error) {
					console.error('Error obteniendo localidad:', error);
					newLocation.locality = 'Ubicación desconocida';
				}

				setLocation(newLocation);
				setLoading(false);
				localStorage.setItem('userLocation', JSON.stringify(newLocation));
			},
			(error) => {
				console.error('Error obteniendo ubicación:', error);
				let errorMessage = 'Error al obtener la ubicación';
				switch (error.code) {
					case error.PERMISSION_DENIED:
						errorMessage = 'Permiso de ubicación denegado';
						break;
					case error.POSITION_UNAVAILABLE:
						errorMessage = 'Información de ubicación no disponible';
						break;
					case error.TIMEOUT:
						errorMessage = 'Tiempo de espera agotado';
						break;
				}
				setError(errorMessage);
				setLoading(false);
				const defaultLocation: Location = { lat: -34.6118, lng: -58.3960, locality: 'Buenos Aires' };
				setLocation(defaultLocation);
			},
			{ enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 }
		);
	};

	useEffect(() => {
		// Cargar ubicación guardada y completar localidad si falta
		const savedLocation = localStorage.getItem('userLocation');
		if (savedLocation) {
			try {
				const parsed: Location = JSON.parse(savedLocation);
				setLocation(parsed);
				if (parsed && parsed.lat && parsed.lng && !parsed.locality) {
					getLocalityFromCoordinates(parsed.lat, parsed.lng)
						.then((locName) => {
							const updated = { ...parsed, locality: locName };
							setLocation(updated);
							localStorage.setItem('userLocation', JSON.stringify(updated));
						})
						.catch(() => {});
				}
			} catch (e) {
				console.error('Error parsing saved location:', e);
			}
		}
	}, []);

	const value: LocationContextType = { location, loading, error, requestLocation };

	return <LocationContext.Provider value={value}>{children}</LocationContext.Provider>;
};
