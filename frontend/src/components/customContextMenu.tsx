import { Form, Modal } from "antd";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { openNotification } from "utils/utilities";
import {
  closeContextMenu,
  openContextMenu,
} from "../redux/actions/contextMenuActions";

export const customContextMenu = (
  event: $TSFixMe,
  record: $TSFixMe,
  menu: $TSFixMe,
  dispatch: $TSFixMe
) => {
  event?.preventDefault();
  dispatch(openContextMenu());
};

const Popup = (contextMenuState: {
  event: $TSFixMe;
  record: $TSFixMe;
  menu: $TSFixMe;
  dispatch: $TSFixMe;
}) => {
  const contextMenuStateRedux = useSelector(
    (state) => (state as $TSFixMe).contextMenu
  );
  const dispatch = useDispatch();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [item, setItem] = useState<any>();

  const [form] = Form.useForm();
  const onFinish = (values: $TSFixMe) => {
    item.action(contextMenuState.record, values);
  };
  function onClickOutside() {
    dispatch(closeContextMenu());
  }

  if (!contextMenuStateRedux.visible) {
    document.removeEventListener("click", onClickOutside);
  } else {
    document.addEventListener("click", onClickOutside);
  }
  return (
    <>
      <Modal
        title={isModalVisible ? item.item : ""}
        open={isModalVisible}
        onOk={() => {
          setIsModalVisible(false);
          form.submit();
        }}
        onCancel={() => setIsModalVisible(false)}
        styles={{
          mask: {
            backgroundColor: "rgba(248, 250, 251, 0.7)",
          },
        }}
      >
        <Form name="contextMenuForm" form={form} onFinish={onFinish}>
          {isModalVisible && item.modal}
        </Form>
      </Modal>

      {contextMenuStateRedux.visible && (
        <ul
          className="popup"
          style={{
            left: `${contextMenuState.event.clientX}px`,
            top: `${contextMenuState.event.clientY}px`,
            width: "170px",
          }}
        >
          {contextMenuState.menu.map((menuItem: $TSFixMe) => (
            <>
              <li
                className={`${
                  menuItem.disabled
                    ? "disabled-context-menu-item"
                    : "enabled-context-menu-item"
                }`}
                onClick={() => {
                  setItem(menuItem);
                  menuItem.disabled
                    ? openNotification(
                        "Please select an appropriate option",
                        "Option not available",
                        "warning"
                      )
                    : menuItem.modal
                    ? setIsModalVisible(true)
                    : menuItem.action !== "" &&
                      menuItem.action(contextMenuState.record);
                }}
              >
                {menuItem.icon} {menuItem.item}
              </li>
            </>
          ))}
        </ul>
      )}
    </>
  );
};

export default Popup;
