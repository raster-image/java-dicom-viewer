// Type declarations for Cornerstone.js packages without official types

declare module '@cornerstonejs/dicom-image-loader' {
  export const external: {
    cornerstone: any;
    dicomParser: any;
  };
  export const webWorkerManager: {
    initialize: (config: any) => void;
  };
  export const wadouri: {
    fileManager: {
      add: (file: File) => string;
    };
    dataSetCacheManager: any;
  };
  export const wadors: any;
  export function configure(options: any): void;
  export function createImage(imageId: string, pixelData: any): any;
}
