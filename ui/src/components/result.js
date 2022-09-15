import React from 'react';
import PropTypes from "prop-types";
import Table from 'react-bootstrap/Table';

class Result extends React.Component {
    render() {
        const result = this.props.result;
        const analysis = [];
        result.forEach(res => {
            analysis.push(
                <tr key={res.id}><td>{res.word}</td><td>{res.count}</td></tr>
            )
        })

    return(
        <Table striped bordered hover variant="dark">
            <thead>
                <tr>
                    <th>Word</th>
                    <th>Count</th>
                </tr>
            </thead>
            <tbody>
                {analysis}
            </tbody>
        </Table>
    )}
}

export default Result;

Result.propType = {
    result: PropTypes.object.isRequired
}