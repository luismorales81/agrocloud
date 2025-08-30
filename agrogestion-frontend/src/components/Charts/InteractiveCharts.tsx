import React, { useState, useEffect } from 'react';

interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string | string[];
    borderWidth?: number;
  }[];
}

interface ChartProps {
  type: 'line' | 'bar' | 'pie' | 'doughnut';
  data: ChartData;
  title: string;
  height?: number;
  showLegend?: boolean;
  responsive?: boolean;
}

const InteractiveCharts: React.FC<ChartProps> = ({
  type,
  data,
  title,
  height = 300,
  showLegend = true,
  responsive = true
}) => {
  const [chartData, setChartData] = useState<ChartData>(data);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  useEffect(() => {
    setChartData(data);
  }, [data]);

  const getRandomColor = (alpha: number = 0.8): string => {
    const colors = [
      `rgba(54, 162, 235, ${alpha})`,
      `rgba(255, 99, 132, ${alpha})`,
      `rgba(75, 192, 192, ${alpha})`,
      `rgba(255, 205, 86, ${alpha})`,
      `rgba(153, 102, 255, ${alpha})`,
      `rgba(255, 159, 64, ${alpha})`,
      `rgba(199, 199, 199, ${alpha})`,
      `rgba(83, 102, 255, ${alpha})`,
      `rgba(78, 252, 3, ${alpha})`,
      `rgba(252, 3, 244, ${alpha})`
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  };

  const generateColors = (count: number): string[] => {
    return Array.from({ length: count }, () => getRandomColor());
  };

  const renderLineChart = () => {
    const colors = generateColors(chartData.datasets.length);
    
    return (
      <div style={{ position: 'relative', height: `${height}px` }}>
        <svg width="100%" height="100%" style={{ overflow: 'visible' }}>
          {/* Grid lines */}
          {Array.from({ length: 5 }, (_, i) => (
            <line
              key={`grid-${i}`}
              x1="0"
              y1={((i + 1) * height) / 5}
              x2="100%"
              y2={((i + 1) * height) / 5}
              stroke="#e5e7eb"
              strokeWidth="1"
            />
          ))}
          
          {/* Data lines */}
          {chartData.datasets.map((dataset, datasetIndex) => {
            const maxValue = Math.max(...dataset.data);
            const minValue = Math.min(...dataset.data);
            const range = maxValue - minValue;
            
            const points = dataset.data.map((value, index) => {
              const x = (index / (dataset.data.length - 1)) * 100;
              const y = 100 - ((value - minValue) / range) * 80;
              return { x, y, value };
            });

            return (
              <g key={`dataset-${datasetIndex}`}>
                {/* Line */}
                <polyline
                  points={points.map(p => `${p.x}%,${p.y}%`).join(' ')}
                  fill="none"
                  stroke={colors[datasetIndex]}
                  strokeWidth="3"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                
                {/* Points */}
                {points.map((point, pointIndex) => (
                  <circle
                    key={`point-${datasetIndex}-${pointIndex}`}
                    cx={`${point.x}%`}
                    cy={`${point.y}%`}
                    r="4"
                    fill={colors[datasetIndex]}
                    stroke="white"
                    strokeWidth="2"
                    style={{ cursor: 'pointer' }}
                    onMouseEnter={() => setHoveredIndex(pointIndex)}
                    onMouseLeave={() => setHoveredIndex(null)}
                  />
                ))}
              </g>
            );
          })}
        </svg>
        
        {/* Tooltip */}
        {hoveredIndex !== null && (
          <div
            style={{
              position: 'absolute',
              background: 'rgba(0, 0, 0, 0.8)',
              color: 'white',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              pointerEvents: 'none',
              zIndex: 1000
            }}
          >
            <div>{chartData.labels[hoveredIndex]}</div>
            {chartData.datasets.map((dataset, index) => (
              <div key={index} style={{ color: generateColors(1)[0] }}>
                {dataset.label}: {dataset.data[hoveredIndex]}
              </div>
            ))}
          </div>
        )}
      </div>
    );
  };

  const renderBarChart = () => {
    const colors = generateColors(chartData.datasets[0].data.length);
    const maxValue = Math.max(...chartData.datasets[0].data);
    const barWidth = 100 / chartData.datasets[0].data.length;
    
    return (
      <div style={{ position: 'relative', height: `${height}px` }}>
        <svg width="100%" height="100%">
          {/* Grid lines */}
          {Array.from({ length: 5 }, (_, i) => (
            <line
              key={`grid-${i}`}
              x1="0"
              y1={((i + 1) * height) / 5}
              x2="100%"
              y2={((i + 1) * height) / 5}
              stroke="#e5e7eb"
              strokeWidth="1"
            />
          ))}
          
          {/* Bars */}
          {chartData.datasets[0].data.map((value, index) => {
            const barHeight = (value / maxValue) * (height * 0.8);
            const x = (index * barWidth) + (barWidth * 0.1);
            const y = height - barHeight - 20;
            
            return (
              <g key={`bar-${index}`}>
                <rect
                  x={`${x}%`}
                  y={y}
                  width={`${barWidth * 0.8}%`}
                  height={barHeight}
                  fill={colors[index]}
                  style={{ cursor: 'pointer' }}
                  onMouseEnter={() => setHoveredIndex(index)}
                  onMouseLeave={() => setHoveredIndex(null)}
                />
                <text
                  x={`${x + barWidth * 0.4}%`}
                  y={height - 5}
                  textAnchor="middle"
                  fontSize="12"
                  fill="#666"
                >
                  {chartData.labels[index]}
                </text>
              </g>
            );
          })}
        </svg>
        
        {/* Tooltip */}
        {hoveredIndex !== null && (
          <div
            style={{
              position: 'absolute',
              background: 'rgba(0, 0, 0, 0.8)',
              color: 'white',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              pointerEvents: 'none',
              zIndex: 1000
            }}
          >
            <div>{chartData.labels[hoveredIndex]}</div>
            <div>Valor: {chartData.datasets[0].data[hoveredIndex]}</div>
          </div>
        )}
      </div>
    );
  };

  const renderPieChart = () => {
    const colors = generateColors(chartData.datasets[0].data.length);
    const total = chartData.datasets[0].data.reduce((sum, value) => sum + value, 0);
    const centerX = 50;
    const centerY = 50;
    const radius = 40;
    
    let currentAngle = 0;
    const slices = chartData.datasets[0].data.map((value, index) => {
      const percentage = value / total;
      const angle = percentage * 360;
      const startAngle = currentAngle;
      currentAngle += angle;
      
      const startRadians = (startAngle * Math.PI) / 180;
      const endRadians = (currentAngle * Math.PI) / 180;
      
      const x1 = centerX + radius * Math.cos(startRadians);
      const y1 = centerY + radius * Math.sin(startRadians);
      const x2 = centerX + radius * Math.cos(endRadians);
      const y2 = centerY + radius * Math.sin(endRadians);
      
      const largeArcFlag = angle > 180 ? 1 : 0;
      
      const pathData = [
        `M ${centerX} ${centerY}`,
        `L ${x1} ${y1}`,
        `A ${radius} ${radius} 0 ${largeArcFlag} 1 ${x2} ${y2}`,
        'Z'
      ].join(' ');
      
      return {
        pathData,
        color: colors[index],
        label: chartData.labels[index],
        value,
        percentage: (percentage * 100).toFixed(1),
        centerAngle: startAngle + angle / 2
      };
    });
    
    return (
      <div style={{ position: 'relative', height: `${height}px` }}>
        <svg width="100%" height="100%" viewBox="0 0 100 100">
          {slices.map((slice, index) => (
            <g key={`slice-${index}`}>
              <path
                d={slice.pathData}
                fill={slice.color}
                stroke="white"
                strokeWidth="1"
                style={{ cursor: 'pointer' }}
                onMouseEnter={() => setHoveredIndex(index)}
                onMouseLeave={() => setHoveredIndex(null)}
              />
              
              {/* Labels */}
              <text
                x={centerX + (radius * 0.7) * Math.cos((slice.centerAngle * Math.PI) / 180)}
                y={centerY + (radius * 0.7) * Math.sin((slice.centerAngle * Math.PI) / 180)}
                textAnchor="middle"
                dominantBaseline="middle"
                fontSize="3"
                fill="white"
                fontWeight="bold"
              >
                {slice.percentage}%
              </text>
            </g>
          ))}
        </svg>
        
        {/* Legend */}
        {showLegend && (
          <div style={{ 
            position: 'absolute', 
            bottom: '10px', 
            left: '10px', 
            right: '10px',
            display: 'flex',
            flexWrap: 'wrap',
            gap: '8px'
          }}>
            {slices.map((slice, index) => (
              <div
                key={`legend-${index}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  fontSize: '11px'
                }}
              >
                <div
                  style={{
                    width: '12px',
                    height: '12px',
                    backgroundColor: slice.color,
                    borderRadius: '2px'
                  }}
                />
                <span>{slice.label}</span>
              </div>
            ))}
          </div>
        )}
        
        {/* Tooltip */}
        {hoveredIndex !== null && (
          <div
            style={{
              position: 'absolute',
              background: 'rgba(0, 0, 0, 0.8)',
              color: 'white',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              pointerEvents: 'none',
              zIndex: 1000
            }}
          >
            <div>{slices[hoveredIndex].label}</div>
            <div>Valor: {slices[hoveredIndex].value}</div>
            <div>Porcentaje: {slices[hoveredIndex].percentage}%</div>
          </div>
        )}
      </div>
    );
  };

  const renderChart = () => {
    switch (type) {
      case 'line':
        return renderLineChart();
      case 'bar':
        return renderBarChart();
      case 'pie':
      case 'doughnut':
        return renderPieChart();
      default:
        return <div>Tipo de gráfico no soportado</div>;
    }
  };

  return (
    <div style={{
      background: 'white',
      borderRadius: '8px',
      padding: '20px',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      height: responsive ? 'auto' : `${height + 60}px`
    }}>
      <h3 style={{
        margin: '0 0 20px 0',
        fontSize: '18px',
        fontWeight: 'bold',
        color: '#374151'
      }}>
        {title}
      </h3>
      
      {renderChart()}
    </div>
  );
};

export default InteractiveCharts;
