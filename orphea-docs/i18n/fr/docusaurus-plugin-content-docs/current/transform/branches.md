# Branches

## Aperçu

Orphea permet un système de contrôle de version distribué qui permet aux développeurs de travailler simultanément sur plusieurs versions d'une base de code. Les branches fournissent un moyen d'isoler le travail sur une fonctionnalité particulière ou un correctif sans affecter la base de code principale.

En général, les branches Orphea permettent aux développeurs de travailler indépendamment sur différentes parties d'une base de code, de collaborer sur des fonctionnalités et des corrections de bugs, et de suivre les modifications apportées à la base de code au fil du temps.

Voici une brève explication d'une branche :

Branche principale: La branche master est la branche principale du référentiel Orphea. Elle contient généralement la dernière version stable de la base de code.

## Comment créer une branche :

- Connectez-vous à votre compte
- Accédez à Projets à l'aide du menu de la barre latérale
- Sélectionnez votre projet actif
- Sélectionnez votre référentiel

Voici à quoi ressemblerait votre page :

![Branches](../_media/Transform/Branches/branches%20fr.png)

Par défaut, Orphea vous montrera la branche principale.

Pour créer une nouvelle branche, cliquez sur le logo de la branche de l'arbre :

![NewBranch](<../_media/Transform/Branches/newbranch%20fr%20(2).png>)

:::note
Vous pouvez nommer la branche selon la fonction précise en minuscules uniquement.
:::

![NewBranch1](../_media/Transform/Branches/newbranch2%20fr.png)

Et une nouvelle branche pour travailler sur une fonctionnalité spécifique, une amélioration est créée.

:::note
Vous ne pouvez pas supprimer la branche principale.
:::

![NewBranch2](../_media/Transform/Branches/devbranch%20fr.png)

Vous pouvez extraire le dernier code de l'une ou l'autre des branches si nécessaire.

## Comment fusionner une ou plusieurs branches

Vous pouvez fusionner une ou plusieurs branches afin que les modifications apportées dans une branche soient ajoutées aux modifications apportées dans une autre branche. Cela se fait généralement lorsqu'un développeur souhaite incorporer les modifications qu'il a apportées dans une branche de fonctionnalités ou une branche de correction de bugs dans la branche principale (généralement la branche principale).

Sélectionnez la branche que vous souhaitez fusionner avec la branche principale, et elle sera fusionnée dès que les modifications seront validées.

![MergeBranch](../_media/Transform/Branches/mergebranch%20fr.png)

Vous pouvez fusionner plusieurs branches dans la branche principale.

Considérez la branche master comme la branche constante.

## Comment supprimer des branches

Une fois qu'un développeur a terminé de travailler sur différentes parties d'une base de code, en corrigeant des bugs, il peut supprimer cette branche si elle ne lui sert pas à des fins futures.

- Pour supprimer une branche particulière, cliquez sur le menu déroulant de la branche et appuyez sur l'icône de corbeille pour supprimer la branche.

![DeleteBranch](../_media/Transform/Branches/delete%20fr.png)

- Orphea vous demandera de confirmer le nom de la branche que vous souhaitez supprimer.

![ConfirmDeletion](../_media/Transform/Branches/delete1.1%20fr.png)
