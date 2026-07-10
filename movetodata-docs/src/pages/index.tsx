import React, { useEffect } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';
import axios from 'axios';
import Translate from '@docusaurus/Translate';

import { getTheme } from '../components/utils'

import usePrefersColorScheme from "use-prefers-color-scheme";


async function Ping() {
  const BASE_URL = location.protocol + '//' + location.host;

  if (BASE_URL == "http://localhost:3000") {
    console.log("on local env, no need to check auth.")
  } else {
    const cookies = document.cookie.split('; ');
    let movetodataToken = '';

    for (const cookie of cookies) {
        if (cookie.startsWith('bAT=')) {
            movetodataToken = cookie.split('=')[1];
            break;
        }
    }

    const config = {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${movetodataToken}`,
      },
    };

    try {
      const { data } = await axios.get(`${BASE_URL}/api/ping`, config);

      if (data.message === "pong") {
        // console.log("session valid");
      } else {
        // console.log("Unauthorized");
        window.location.replace(BASE_URL);
      }
    } catch (error) {
      if ((error).code === "ERR_NETWORK") {
        // console.log("Networ Error");
        window.location.replace(BASE_URL);
      } else if (
        (error as { [id: string]: any }).response?.data.error === "Unauthorized"
      ) {
        // console.log("Unauthorized");
        window.location.replace(BASE_URL);
      }
    }
  }
}

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <h1 className="hero__title"><Translate>{siteConfig.title}</Translate></h1>
        <p className="hero__subtitle"><Translate>{siteConfig.tagline}</Translate></p>
        <div className={styles.buttons}>
          <Link
            className="button button--secondary button--lg"
            to="/docs/getting_started/intro"> <Translate>Let's go</Translate>
          </Link>
        </div>
      </div>
    </header >

  );
}

export default function Home(): JSX.Element {

  const prefersColorScheme = usePrefersColorScheme();

  useEffect(() => {
    Ping();
    const timer = setInterval(function () {
      Ping();
    }, 10000);

    return () => {
      clearInterval(timer);
    };
  }

    , []);

  // useEffect(() => {
  //   if (prefersColorScheme === "dark") {
  //     // document.getElementById("root")?.classList.add("dark");
  //     document.documentElement.setAttribute('data-theme', 'dark');
  //     getTheme(true);
  //   } else {
  //     // document.getElementById("root")?.classList.remove("dark");
  //     document.documentElement.setAttribute('data-theme', 'light');
  //     getTheme(false);
  //   }
  // }, [prefersColorScheme]);

  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title={`MoveToData Documentation ${siteConfig.title}`}
      description="MoveToData platform documentation <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}

