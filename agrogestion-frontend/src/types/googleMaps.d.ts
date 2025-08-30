declare global {
  interface Window {
    google: typeof google;
  }
}

declare namespace google {
  namespace maps {
    class Map {
      constructor(mapDiv: HTMLElement, opts?: MapOptions);
      setCenter(latlng: LatLng | LatLngLiteral): void;
      setZoom(zoom: number): void;
      getCenter(): LatLng;
      getZoom(): number;
    }

    interface MapOptions {
      center?: LatLng | LatLngLiteral;
      zoom?: number;
      mapTypeId?: MapTypeId;
      mapTypeControl?: boolean;
      streetViewControl?: boolean;
      fullscreenControl?: boolean;
      zoomControl?: boolean;
    }

    enum MapTypeId {
      SATELLITE = 'satellite'
    }

    class LatLng {
      constructor(lat: number, lng: number);
      lat(): number;
      lng(): number;
    }

    interface LatLngLiteral {
      lat: number;
      lng: number;
    }

    class Polygon {
      constructor(opts?: PolygonOptions);
      setMap(map: Map | null): void;
      getPath(): MVCArray<LatLng>;
      setOptions(options: PolygonOptions): void;
    }

    interface PolygonOptions {
      paths?: LatLng[] | LatLngLiteral[];
      fillColor?: string;
      fillOpacity?: number;
      strokeWeight?: number;
      strokeColor?: string;
      editable?: boolean;
      draggable?: boolean;
      map?: Map;
    }

    class MVCArray<T> {
      getLength(): number;
      getAt(index: number): T;
      push(element: T): number;
      clear(): void;
    }

    enum ControlPosition {
      TOP_CENTER = 1
    }

    namespace drawing {
      class DrawingManager {
        constructor(opts?: DrawingManagerOptions);
        setMap(map: Map | null): void;
        setDrawingMode(mode: OverlayType | null): void;
      }

      interface DrawingManagerOptions {
        drawingMode?: OverlayType;
        drawingControl?: boolean;
        drawingControlOptions?: DrawingControlOptions;
        polygonOptions?: PolygonOptions;
      }

      interface DrawingControlOptions {
        position?: ControlPosition;
        drawingModes?: OverlayType[];
      }

      enum OverlayType {
        POLYGON = 'polygon'
      }
    }
  }
}

export {};
