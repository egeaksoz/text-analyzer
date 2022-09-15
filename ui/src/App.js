import React from 'react';
import {BrowserRouter as Router, Switch, Route} from 'react-router-dom';
import Main from './components/main';

class App extends React.Component {
    render(){
      return (
        <Router>
            <Switch>
                <Route path="/" exact component={Main} />
            </Switch>
        </Router>
      );
    }
}

export default App;