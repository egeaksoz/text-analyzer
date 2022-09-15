import React from 'react';
import { InputGroup, Button, FormControl, Container, Row, Col } from 'react-bootstrap';
import Result from './result';
import './main.css';
import { analyze, result } from '../action';

class Main extends React.Component {
    constructor() {
        super();
        this.state = { text: "", result: [], showResults: false }
    }

    componentDidUpdate(){
        clearInterval(this.resultTimer);
    }

    sentToAnalysis() {
        analyze(this.state.text);
        this.resultTimer = setInterval(() => {
            result()
                .then(res => this.setState({ result: res, showResults: true }));
        }, 5000)
    }

    render() {
        return (
            <Container>
                <Row className="justify-content-md-center">
                    <Col md={{ span: 8 }}>
                        <InputGroup className="input">
                            <InputGroup.Prepend>
                                <InputGroup.Text> Enter your text here: </InputGroup.Text>
                                <FormControl as="textarea" className="form" aria-label="analyze-text" value={this.state.text} onChange={e => this.setState({ text: e.target.value })} />
                            </InputGroup.Prepend>
                        </InputGroup>
                        <Button type="button" id="analyze" onClick={() => this.sentToAnalysis()} variant="dark" block>Analyze</Button>
                    </Col>
                </Row>
                <Row>
                    <Col md={{ span: 4, offset: 4 }}>
                        {this.state.showResults ? <Result result={this.state.result} /> : null}
                    </Col>
                </Row>
            </Container>
        );
    }
}

export default Main;
