import React from "react";
import {shallow, mount} from "enzyme";
import Enzyme from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import { render } from '@testing-library/react';
import { EditUser } from '/.components/Users/EditUser';


Enzyme.configure({ adapter: new Adapter() });

describe("<EditUser /> component rendered", () => {
    it("should render 1 <EditUser /> component", ()=>{
        const component = shallow(<EditUser />);
        expect(component).toHaveLength(1);
         
        });   
})