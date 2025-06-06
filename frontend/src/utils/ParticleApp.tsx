import React, { useCallback } from "react";
import Particles from "react-tsparticles";
import { loadFull } from "tsparticles";
import type { Container, Engine } from "tsparticles-engine";

interface TProps {
  fullScreen: boolean;
  height: number;
  width: number;
  numberOfParticles: number;
  speed: number;
  logoSize: number;
  image: string;
}

const ParticleApp = ({
  fullScreen,
  height,
  width,
  numberOfParticles,
  speed,
  logoSize,
  image,
}: TProps) => {
  const particlesInit = useCallback(async (engine: Engine) => {
    // you can initialize the tsParticles instance (engine) here, adding custom shapes or presets
    // this loads the tsparticles package bundle, it's the easiest method for getting everything ready
    // starting from v2 you can add only the features you need reducing the bundle size
    await loadFull(engine);
  }, []);

  const particlesLoaded = useCallback(
    async (container: Container | undefined) => {},
    []
  );
  return (
    <Particles
      id="tsparticles"
      init={particlesInit}
      loaded={particlesLoaded}
      options={{
        fullScreen: { enable: fullScreen },
        particles: {
          shape: {
            type: "image",
            image: {
              src: image,
              width: width,
              height: height,
            },
          },
          number: {
            value: numberOfParticles,
            density: {
              enable: true,
              value_area: 800,
            },
          },
          color: {
            value: "#495c6c",
          },
          // shape: {
          //   type: "circle",
          // },
          opacity: {
            value: 1,
          },
          size: {
            value: logoSize,
            random: true,
          },
          line_linked: {
            enable: true,
            distance: 100,
            color: "#94a6b3",
            opacity: 0.5,
            width: 0.4,
          },
          move: {
            enable: true,
            speed: speed, // Reduced speed for slower movement
            direction: "none",
            random: true,
            straight: false,
            out_mode: "bounce",
            bounce: false,
            attract: {
              enable: false, // You can disable attraction to avoid particles moving too deliberately towards each other
            },
          },
        },
        interactivity: {
          detect_on: "canvas",
          events: {
            onhover: {
              enable: true,
              mode: "grab", // Changed to 'grab' to avoid scattering on hover
            },
            onclick: {
              enable: true,
              mode: "push",
            },
            resize: true,
          },
          modes: {
            grab: {
              distance: 140, // Increased distance for 'grab' effect
              line_linked: {
                opacity: 0.5,
              },
            },
            bubble: {
              distance: 400,
              size: 40,
              duration: 2,
              opacity: 8,
              speed: 3,
            },
            repulse: {
              distance: 200,
              duration: 0.4,
            },
            push: {
              particles_nb: 4,
            },
            remove: {
              particles_nb: 2,
            },
          },
        },
        retina_detect: true,
      }}
      height="100%"
      width="100%"
    />
  );
};

export { ParticleApp };
