import { fetchBlobFileAPI } from "components/BlobViewer/BlobViewer.api";
import type { LexicalEditor, NodeKey } from "lexical";

import * as React from "react";
import { Suspense, useRef } from "react";

const imageCache = new Set();

function useSuspenseImage(src: string) {
  if (!imageCache.has(src)) {
    throw new Promise((resolve) => {
      const img = new Image();
      img.src = src;
      img.onload = () => {
        imageCache.add(src);
        resolve(null);
      };
    });
  }
}

function LazyImage({
  altText,
  className,
  imageRef,
  src,
  width,
  height,
  maxWidth,
}: {
  altText: string;
  className: string | null;
  height: "inherit" | number;
  imageRef: { current: null | HTMLImageElement };
  maxWidth: number;
  src: string;
  width: "inherit" | number;
}): JSX.Element {
  // useSuspenseImage(src);
  return (
    <img
      className={className || undefined}
      src={src}
      alt={altText}
      ref={imageRef}
      style={{
        height,
        maxWidth,
        width,
      }}
    />
  );
}

export default function ImageComponent({
  src,
  altText,
  width,
  height,
  maxWidth,
}: {
  altText: string;
  caption: LexicalEditor;
  height: "inherit" | number;
  maxWidth: number;
  nodeKey: NodeKey;
  resizable: boolean;
  showCaption: boolean;
  src: string;
  width: "inherit" | number;
  captionsEnabled: boolean;
}): JSX.Element {
  const imageRef = useRef<null | HTMLImageElement>(null);
  const [blob, setBlob] = React.useState<any>("");

  React.useEffect(() => {
    fetchBlobFileAPI(src as string).then((response) => {
      if (response.status === 200) {
        const blob = new Blob([response.data], {
          type: "application/octet-stream",
        });

        setBlob(window.URL.createObjectURL(blob));
      }
    });
  }, []);

  return (
    <Suspense fallback={null}>
      <>
        <div>
          <LazyImage
            className=""
            src={blob}
            altText={altText}
            imageRef={imageRef}
            width={width}
            height={height}
            maxWidth={maxWidth}
          />
        </div>
      </>
    </Suspense>
  );
}
