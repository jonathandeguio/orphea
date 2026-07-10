# In Unify

cd Unify/helm

```
./build.sh tycho
```

else image already exists

  ``` 
  source env-...sh
  
  cd repos/tycho/helm/superset
  modify values.yaml for PROJECT_ID
  sed -e "s/movetodata-334213/$PROJECT_ID/g" values.yaml > new-values.yaml
  mv new-values.yaml values.yaml
  ./install_helm_superset.sh
  ```


***Login to kepler***  

Add database with connection string

postgresql://postgres:secret@boson-db/kepler


