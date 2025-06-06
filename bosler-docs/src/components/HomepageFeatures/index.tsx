import React, { useEffect } from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';
import Translate from '@docusaurus/Translate';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Ingestion',
    Svg: require('@site/static/img/undraw_data_processing.svg').default,
    description: (
      <>
        <Translate>
          Ingest customer data using various connectors such as Folder, JDBC, API
        </Translate>
      </>
    ),
  },
  {
    title: 'Transformation',
    Svg: require('@site/static/img/undraw_online_connection.svg').default,
    description: (
      <>
        <Translate>
          Transform data and create pipelines.
        </Translate>
      </>
    ),
  },
  {
    title: 'Visualisation',
    Svg: require('@site/static/img/undraw_data_trends.svg').default,
    description: (
      <>
        <Translate>
          Analyze data using Charts, Graphs, Dashboard
        </Translate>
      </>
    ),
  },
];

function Feature({ title, Svg, description }: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />

        <div className="text--center padding-horiz--md">
          <h3>{title}</h3>
          <p>{description}</p>
        </div></div></div>

  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}

        </div></div>
    </section>
  );
}
