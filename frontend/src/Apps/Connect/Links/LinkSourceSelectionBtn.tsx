import { Col, Row, Typography } from "antd";
import { SourceIcon } from "assets/icons/boslerDataIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { useDispatch } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getLanguageLabel, getSourceIcon, notEmpty } from "utils/utilities";
import { openFileExplorerModal } from "../../../redux/ModalSlice";
import { ISourceConfig } from "../Sources/Source";
import { SourceButtonPopover } from "./SourceButtonPopover";
const { Text } = Typography;
interface IProps {
  selectedSource: ISourceConfig | undefined;
  defaultParent: any;
  addSourceDetails: any;
}
const LinkSourceSelectionBtn = ({
  selectedSource,
  defaultParent,
  addSourceDetails,
}: IProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  return (
    <>
      <Text type="secondary" strong>
        {getLanguageLabel("dataSource").toUpperCase()}
      </Text>
      <Row
        gutter={[16, 16]}
        style={{
          marginTop: "5px",
        }}
      >
        <Col span={8}>
          <Text>{getLanguageLabel("dataSource")}</Text>
        </Col>
        <Col span={16}>
          <SourceButtonPopover source={selectedSource}>
            <BoslerButton
              icon={
                selectedSource ? (
                  getSourceIcon(
                    (selectedSource as $TSFixMe)["type"],
                    (selectedSource as $TSFixMe)["dbmsType"]
                  )
                ) : (
                  <SourceIcon />
                )
              }
              onClick={() => {
                dispatch(
                  openFileExplorerModal({
                    type: ["SOURCE"],
                    action: (data) => {
                      addSourceDetails(data);
                    },
                    activeId: defaultParent,
                  })
                );
              }}
              intent={notEmpty(selectedSource) ? "success" : "warning"}
            >
              {notEmpty(selectedSource)
                ? (selectedSource as any)?.name
                : "Select Source"}
            </BoslerButton>
          </SourceButtonPopover>
        </Col>
      </Row>
    </>
  );
};

export default LinkSourceSelectionBtn;
