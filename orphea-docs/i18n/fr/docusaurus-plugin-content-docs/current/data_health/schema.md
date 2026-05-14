# Schéma

#### Objectif

Les vérifications du schéma visent à garantir que le schéma de l'ensemble de données correspond à la structure attendue. Elles assurent la conformité des noms de colonnes, des types de données et des contraintes.

#### Configuration

Pour configurer une vérification du schéma, vous devez définir le schéma attendu et le comparer avec le schéma actuel. Voici un exemple de configuration :

**Exemple de Vérification du Schéma**

- **Nom:** Validation du Schéma
- **Type:** Schéma
- **Critères:**
  - Correspondre au Schéma Attendu:
    - Colonne: ID, Type: Entier
    - Colonne: Nom, Type: Chaîne
    - Colonne: Valeur, Type: Flottant

#### Surveillance

Les vérifications du schéma peuvent être surveillées via le tableau de bord de l'application. Les alertes peuvent être configurées pour notifier les parties prenantes en cas de détection de divergences dans le schéma.

#### Bonnes Pratiques

- **Schéma bien défini:** Définissez un schéma clair et précis pour chaque ensemble de données.
- **Surveillance continue:** Utilisez des outils de surveillance continue pour détecter les divergences en temps réel.
- **Documentation:** Documentez les schémas pour faciliter la maintenance et les mises à jour.
