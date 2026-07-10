# Architecture

MoveToData est une architecture conçue pour répondre à tous les types d'utilisateurs, gérer les charges de travail basées sur les données à forte demande et s'adapter à divers substrats d'infrastructure.

Comme MoveToData a gagné en importance pour de nombreuses organisations, il a été important de nous assurer que :

- La mise à l'échelle automatique des services MoveToData et du maillage de calcul associé est basée sur un paradigme de conteneurisation cohérent. Ceci est réalisé à l'aide du moteur Kubernetes Docker, qui alimente toute l'infrastructure de mise à l'échelle automatique de MoveToData.

- Les mises à niveau vers MoveToData sont effectuées sans aucun temps d'arrêt, avec une surveillance détaillée pour guider les stratégies de mise à niveau, surveiller les progrès et revenir en arrière si nécessaire.

- La sécurité et le traçage font partie intégrante de chaque aspect de MoveToData et sont constamment maintenues dans toute l'architecture de la plateforme. Cela garantit qu'aucun service ou utilisateur n'est responsable de l'application des politiques de sécurité de l'organisation ou du suivi de la provenance des données. Des données à la prise de décision, les services de base de MoveToData sont conçus pour mettre en œuvre, appliquer et surveiller les politiques de gouvernance qui sont configurées, synchronisées et héritées. De cette façon, la plateforme peut garantir que toutes les opérations sont conformes aux normes de l'entreprise en matière de sécurité et de gouvernance des données.

- Nous nous efforçons de nous assurer que les langages open source les plus largement utilisés sont accessibles de manière sécurisée et cohérente dans les paradigmes pilotés par code. Cela inclut des langages tels que Python, SQL et R ; Python et R pour les workflows d'apprentissage automatique.

- Tous les services de MoveToData sont configurés de manière hautement disponible et redondante. Cela inclut non seulement les services backend de base, mais également les services d'application frontaux, les outils de visualisation, les exécutions de build et tous les services utilisés par les différents utilisateurs.
- MoveToData est conçu pour être indépendant du stockage, ce qui signifie qu'il peut fonctionner avec diverses technologies de stockage à différents niveaux de l'architecture. Cela inclut l'utilisation du stockage blob ou HDFS, S3. Cette flexibilité permet à MoveToData de s'adapter aux besoins de stockage spécifiques d'une organisation.

- Le calcul de MoveToData est principalement basé sur Apache Spark, une technologie éprouvée pour les données à grande échelle.
