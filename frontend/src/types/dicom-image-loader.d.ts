declare module '@cornerstonejs/dicom-image-loader' {
  export const external: {
    cornerstone: unknown;
    dicomParser: unknown;
  };

  export const webWorkerManager: {
    initialize: (config: {
      maxWebWorkers?: number;
      startWebWorkersOnDemand?: boolean;
      taskConfiguration?: {
        decodeTask?: {
          initializeCodecsOnStartup?: boolean;
          strict?: boolean;
        };
      };
    }) => void;
  };

  export function wadouri(imageId: string): Promise<unknown>;
  export function wadors(imageId: string): Promise<unknown>;
}
