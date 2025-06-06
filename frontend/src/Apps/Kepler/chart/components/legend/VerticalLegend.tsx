import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { isDefined } from "utils/utilities";
import { updateCustomize } from "../../../../../redux/actions/keplerActions";
import { getRelativeFontSize } from "../../chartOptionsFactory";
import { Legend } from "./HorizontalLegend";
import { ColorBlock } from "./SubComponents";

export const VerticalLegend: React.FC<Legend> = ({
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
    <div
      style={{
        fontSize: getRelativeFontSize(16, dimensions),
        // flexBasis: getRelativeFontSize(16, dimensions),
      }}
      className={`verticalLegend verticalLegend_${align}`}
    >
      {data.map((series) => (
        <div className="seriesLegend">
          {(data.length > 1 || series.groupBy.length > 0) && (
            <>
              <div className="seriesLegend_meta">
                {data.length > 1 && (
                  <div className="seriesName">{series.seriesName}</div>
                )}
                {series.groupBy.length > 0 && (
                  <div className="groupBy">{series.groupBy.join(", ")}</div>
                )}
              </div>
            </>
          )}

          <div className="legendItem_group">
            {series.items.map((item) => (
              <div
                onClick={() => {
                  if (series.groupBy.length > 0) {
                    const splitted = item.name.split(", ");
                    const filters = series.groupBy.map((g, i) => ({
                      columnName: g,
                      value: splitted[i],
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
                  <BoslerTypography>
                    {customLabel?.[item.name] ?? item.name}
                  </BoslerTypography>
                )}
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};
