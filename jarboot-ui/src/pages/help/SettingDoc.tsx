import React, {memo} from "react";
import {BackTop, Breadcrumb, Typography} from "antd";
import {useIntl} from "umi";
import {HomeOutlined} from "@ant-design/icons";

const { Title, Paragraph, Text } = Typography;

const SettingDoc = memo(() => {
    const intl = useIntl();
    return <>
        <Breadcrumb>
            <Breadcrumb.Item><HomeOutlined /></Breadcrumb.Item>
            <Breadcrumb.Item>
                {intl.formatMessage({id: 'HELP'})}
            </Breadcrumb.Item>
            <Breadcrumb.Item>
                {intl.formatMessage({id: 'SETTING'})}
            </Breadcrumb.Item>
        </Breadcrumb>
        <Typography>
            <Title>{intl.formatMessage({id: 'SETTING'})}</Title>
            <Paragraph>
                <Title level={2}>{intl.formatMessage({id: 'SETTING_P1'})}</Title>
                <ul>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'SERVERS_PATH'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P2'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'DEFAULT_VM_OPT'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P3'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'AUTO_START_AFTER_INIT'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P4'})}
                    </li>
                </ul>
            </Paragraph>
            <Paragraph>
                <Title level={2}>{intl.formatMessage({id: 'SERVICES_CONF'})}</Title>
                <ul>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'COMMAND_LABEL'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P6'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'VM_OPT_LABEL'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P7'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'MAIN_ARGS_LABEL'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P8'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'PRIORITY_LABEL'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P9'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'DAEMON_LABEL'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P10'})}
                    </li>
                    <li>
                        <Text keyboard>{intl.formatMessage({id: 'JAR_UPDATE_WATCH_LABEL'})}</Text>
                        {intl.formatMessage({id: 'SETTING_P11'})}
                    </li>
                </ul>
            </Paragraph>
        </Typography>
        <BackTop/>
    </>;
});

export default SettingDoc
