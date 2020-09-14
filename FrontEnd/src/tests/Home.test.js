import React from "react";
import {shallow, mount} from "enzyme";
import Enzyme from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import { render } from '@testing-library/react';
import { Home } from '/.components/Home';


Enzyme.configure({ adapter: new Adapter() });

describe("<Home /> component rendered", () => {
    it("should render 1 <Home /> component", ()=>{
        const component = shallow(<Home />);
        expect(component).toHaveLength(1);
         
        });
})