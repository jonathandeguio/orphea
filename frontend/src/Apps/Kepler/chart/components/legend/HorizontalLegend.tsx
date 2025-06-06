import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { updateCustomize } from "../../../../../redux/actions/keplerActions";
import { isDefined } from "utils/utilities";
import { RootState } from "../../../../../redux/types/store";
import { Dimensions } from "../../ChartComponent/ParentChartComponent";
import { ColorBlock } from "./SubComponents";
import { SeriesLegend } from "./getLegendData";

export interface Legend {
  data: SeriesLegend[];
  onClickLegend: any;
  align: string;
  customLabel: any;
  dimensions: Dimensions;
  editMode: boolean;
}

export const HorizontalLegend: React.FC<Legend> = ({
  data,
  onClickLegend,
  align,
  customLabel,
  editMode,
  dimensions,
}) => {
  const dispatch = useDispatch();
  const customize = useSelector((state: RootState) => state.kepler.customize);

  return (
    <>
      {data.map((series) => (
        <div className="seriesLegend">
          {(data.length > 1 || series.groupBy.length > 0) && (
            <div className="seriesLegend_meta">
              {data.length > 1 && (
                <div className="seriesName">{series.seriesName}</div>
              )}
              {series.groupBy.length > 0 && (
                <div className="groupBy">{series.groupBy.join(", ")}</div>
              )}
            </div>
          )}

          <div className="legendItem_group">
            {series.items.map((item) => (
              <div
                onClick={() => {
                  if (series.groupBy.length > 0) {
                    const splitted = item.name.split(", ");

                    const filters = series.groupBy.map((g, i) => ({
                      columnName: g,
                      FilterValue: splitted[i],
                      operator: "equal",
                      checked: true,
                    }));

                    onClickLegend(filters);
                  }
                }}
                className="legendItem"
              >
                <ColorBlock color={item.color} />{" "}
                {editMode ? (
                  <BoslerInput
                    editText
                    defaultValue={
                      customize?.customLabel?.[item.name] ?? item.name
                    }
                    debounceInterval={1000}
                    onChange={(e) => {
                      if (
                        isDefined(e.target.value) &&
                        e.target.value.length > 0
                      ) {
                        dispatch(
                          updateCustomize({
                            customLabel: {
                              ...customize.customLabel,
                              [item.name]: e.target.value,
                            },
                          })
                        );
                      }
                    }}
                  />
                ) : (
                  customLabel?.[item.name] ?? item.name
                )}
              </div>
            ))}
          </div>
        </div>
      ))}
    </>
  );
};
