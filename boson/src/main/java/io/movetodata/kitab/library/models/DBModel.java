package io.movetodata.kitab.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DBModel extends DatasetModel {
    public int idx;
    public double x;
    public double y;
    public double vx;
    public double vy;
    public boolean branch;  // this was added for bhagesh
    public boolean collapsed;
    public String branchName;

    public String buildStatus;
}
