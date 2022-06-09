import React, {memo} from "react";
import {JarBootConst} from "@/common/JarBootConst";
import CodeEditor from "@/components/code";

const JadView = memo((props: any) => {

    return <>
        <CodeEditor height={JarBootConst.PANEL_HEIGHT - 25}
                    readOnly={true}
                    source={props?.data?.source}/>
    </>
});

export default JadView;
